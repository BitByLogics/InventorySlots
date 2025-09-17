package net.bitbylogic.inventoryslots.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    public static final Config INSTANCE = new Config();

    public boolean enabled = false;

    public float textScale = 0.5f;
    public int textColor = 0xFFFFFFFF;

    public TextAnchor textAnchor = TextAnchor.TOP_LEFT;

    public boolean textShadow = true;

    public static final int[] COLOR_PALETTE = {
            0xFFFFFFFF, // White
            0xFFFF0000, // Red
            0xFF00FF00, // Green
            0xFF0000FF, // Blue
            0xFFFFFF00, // Yellow
            0xFFFF00FF, // Magenta
            0xFF00FFFF, // Cyan
            0xFFFFA500, // Orange
            0xFF800080, // Purple
            0xFFFF69B4, // Pink
            0xFF808080, // Gray
            0xFF000000  // Black
    };

    public static final String[] COLOR_NAMES = {
            "White", "Red", "Green", "Blue", "Yellow",
            "Magenta", "Cyan", "Orange", "Purple", "Pink", "Gray", "Black"
    };

    private static final String CONFIG_FILE = "config/inventory-slots-config.json";

    public enum TextAnchor {
        TOP_LEFT("Top Left"),
        TOP_CENTER("Top Center"),
        TOP_RIGHT("Top Right"),
        CENTER_LEFT("Center Left"),
        CENTER("Center"),
        CENTER_RIGHT("Center Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_CENTER("Bottom Center"),
        BOTTOM_RIGHT("Bottom Right");

        private final String displayName;

        TextAnchor(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
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

    public String getColorName() {
        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            if (COLOR_PALETTE[i] == textColor) {
                return COLOR_NAMES[i];
            }
        }

        return "Custom";
    }

    public void save() {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            JsonObject json = new JsonObject();

            json.addProperty("enabled", enabled);

            json.addProperty("textScale", textScale);
            json.addProperty("textColor", textColor);

            json.addProperty("textAnchor", textAnchor.name());

            json.addProperty("textShadow", textShadow);

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(json, writer);
            }

        } catch (IOException e) {
            System.err.println("[Inventory Slots] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            File configFile = new File(CONFIG_FILE);

            if (!configFile.exists()) {
                save();
                return;
            }

            try (FileReader reader = new FileReader(configFile)) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                if (json != null) {
                    if(json.has("enabled")) {
                        enabled = json.get("enabled").getAsBoolean();
                    }

                    if (json.has("textScale")) {
                        textScale = json.get("textScale").getAsFloat();
                        textScale = Math.max(0.1f, Math.min(1.0f, textScale));
                    }

                    if (json.has("textColor")) {
                        textColor = json.get("textColor").getAsInt();
                        textColor = 0xFF000000 | textColor;
                    }

                    if (json.has("textAnchor")) {
                        String anchorName = json.get("textAnchor").getAsString();

                        try {
                            textAnchor = TextAnchor.valueOf(anchorName);
                        } catch (IllegalArgumentException e) {
                            System.err.println("[Inventory Slots] Invalid anchor value in config: " + anchorName);
                            textAnchor = TextAnchor.TOP_LEFT;
                        }
                    }

                    if (json.has("textShadow")) {
                        textShadow = json.get("textShadow").getAsBoolean();
                    }
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to load config, using defaults: " + e.getMessage());

            enabled = false;

            textScale = 0.5f;
            textColor = 0xFFFFFF;

            textAnchor = TextAnchor.TOP_LEFT;

            textShadow = false;
            save();
        }
    }

}