package net.bitbylogic.inventoryslots.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Config INSTANCE = new Config();

    private final transient Path file;
    public boolean enabled = false;
    public float textScale = 0.5f;
    public int textColor = 0xFFFFFFFF;
    public TextAnchor textAnchor = TextAnchor.TOP_LEFT;
    public boolean textShadow = true;
    public boolean hotbarNumbers = false;
    public boolean hotbarOnly = false;
    public boolean widgetNumbers = false;
    // Defaults preserve the behavior of existing installations.
    public boolean inventoryScreen = true;
    public boolean creativeInventory = true;
    public boolean containerSlots = true;
    public boolean playerInventoryInContainers = true;
    public boolean playerHotbarInContainers = true;
    public Set<String> disabledMenuTypes = new LinkedHashSet<>();
    public Set<ScreenRule> excludedScreens = new LinkedHashSet<>();

    public static final int[] COLOR_PALETTE = {
            0xFFFFFFFF, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF,
            0xFF00FFFF, 0xFFFFA500, 0xFF800080, 0xFFFF69B4, 0xFF808080, 0xFF000000
    };
    public static final String[] COLOR_NAMES = {
            "white", "red", "green", "blue", "yellow", "magenta",
            "cyan", "orange", "purple", "pink", "gray", "black"
    };

    public Config() {
        this(Path.of("config", "inventory-slots-config.json"));
    }

    public Config(Path file) {
        this.file = file;
    }

    public enum ScreenKind { INVENTORY, CREATIVE, CONTAINER }

    /** The title is matched exactly; unrelated chest menus remain enabled. */
    public record ScreenRule(String menuType, String title) { }

    public enum TextAnchor {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

        public String translationKey() {
            return "inventory_slots.anchor." + name().toLowerCase(java.util.Locale.ROOT);
        }

        public float getOffsetX(float textWidth, int slotSize) {
            return switch (this) {
                case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 1;
                case TOP_CENTER, CENTER, BOTTOM_CENTER -> (slotSize - textWidth) / 2;
                case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> slotSize - textWidth - 1;
            };
        }

        public float getOffsetY(float textHeight, int slotSize) {
            return switch (this) {
                case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 1;
                case CENTER_LEFT, CENTER, CENTER_RIGHT -> (slotSize - textHeight) / 2;
                case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> slotSize - textHeight - 1;
            };
        }
    }

    public String colorTranslationKey() {
        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            if (COLOR_PALETTE[i] == textColor) return "inventory_slots.color." + COLOR_NAMES[i];
        }
        return "inventory_slots.color.custom";
    }

    public boolean shouldShowSlot(ScreenKind kind, String menuType, String title,
                                  boolean playerSlot, boolean hotbarSlot) {
        if (!enabled || hotbarOnly || disabledMenuTypes.contains(menuType)
                || excludedScreens.contains(new ScreenRule(menuType, title))) return false;
        return switch (kind) {
            case INVENTORY -> inventoryScreen;
            case CREATIVE -> creativeInventory;
            case CONTAINER -> playerSlot
                    ? (hotbarSlot ? playerHotbarInContainers : playerInventoryInContainers)
                    : containerSlots;
        };
    }

    public boolean toggleScreen(String menuType, String title) {
        ScreenRule rule = new ScreenRule(menuType, title);
        if (excludedScreens.remove(rule)) return true;
        excludedScreens.add(rule);
        return false;
    }

    public boolean toggleMenuType(String menuType) {
        if (disabledMenuTypes.remove(menuType)) return true;
        disabledMenuTypes.add(menuType);
        return false;
    }

    public void containersOnly() {
        enabled = true;
        hotbarOnly = false;
        inventoryScreen = false;
        creativeInventory = false;
        containerSlots = true;
        playerInventoryInContainers = false;
        playerHotbarInContainers = false;
        hotbarNumbers = false;
        widgetNumbers = false;
    }

    public void reset() {
        read(GSON.toJsonTree(new Config(file)).getAsJsonObject());
    }

    public void save() {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[Inventory Slots] Failed to save config: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(file)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null) read(json);
        } catch (IOException | RuntimeException e) {
            // Keep a damaged file for the user to inspect instead of overwriting it.
            System.err.println("[Inventory Slots] Failed to load config: " + e.getMessage());
        }
    }

    private void read(JsonObject json) {
        enabled = value(json, "enabled", enabled, JsonElement::getAsBoolean);
        float scale = value(json, "textScale", textScale, JsonElement::getAsFloat);
        textScale = Float.isFinite(scale) ? Math.max(0.1f, Math.min(1.0f, scale)) : 0.5f;
        textColor = 0xFF000000 | value(json, "textColor", textColor, JsonElement::getAsInt);
        textAnchor = value(json, "textAnchor", textAnchor, v -> TextAnchor.valueOf(v.getAsString()));
        textShadow = value(json, "textShadow", textShadow, JsonElement::getAsBoolean);
        hotbarNumbers = value(json, "hotbarNumbers", hotbarNumbers, JsonElement::getAsBoolean);
        hotbarOnly = value(json, "hotbarOnly", hotbarOnly, JsonElement::getAsBoolean);
        widgetNumbers = value(json, "widgetNumbers", widgetNumbers, JsonElement::getAsBoolean);
        inventoryScreen = value(json, "inventoryScreen", inventoryScreen, JsonElement::getAsBoolean);
        creativeInventory = value(json, "creativeInventory", creativeInventory, JsonElement::getAsBoolean);
        containerSlots = value(json, "containerSlots", containerSlots, JsonElement::getAsBoolean);
        playerInventoryInContainers = value(json, "playerInventoryInContainers", playerInventoryInContainers, JsonElement::getAsBoolean);
        playerHotbarInContainers = value(json, "playerHotbarInContainers", playerHotbarInContainers, JsonElement::getAsBoolean);
        disabledMenuTypes = value(json, "disabledMenuTypes", disabledMenuTypes, v -> {
            Set<String> result = new LinkedHashSet<>();
            for (JsonElement element : v.getAsJsonArray()) result.add(element.getAsString());
            return result;
        });
        excludedScreens = value(json, "excludedScreens", excludedScreens, v -> {
            Set<ScreenRule> result = new LinkedHashSet<>();
            for (JsonElement element : v.getAsJsonArray()) {
                ScreenRule rule = GSON.fromJson(element, ScreenRule.class);
                if (rule != null && rule.menuType() != null && rule.title() != null) result.add(rule);
            }
            return result;
        });
    }

    private static <T> T value(JsonObject json, String key, T fallback, Function<JsonElement, T> parser) {
        if (!json.has(key) || json.get(key).isJsonNull()) return fallback;
        try {
            return parser.apply(json.get(key));
        } catch (RuntimeException e) {
            System.err.println("[Inventory Slots] Ignoring invalid option: " + key);
            return fallback;
        }
    }
}
