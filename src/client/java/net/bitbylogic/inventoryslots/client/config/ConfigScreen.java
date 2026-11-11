package net.bitbylogic.inventoryslots.client.config;

import net.bitbylogic.inventoryslots.client.InventorySlotsClient;
import net.bitbylogic.inventoryslots.client.SlotLabelRenderer;
import net.bitbylogic.inventoryslots.client.SlotVisibility;
import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final Config config = Config.INSTANCE;

    private int tab;
    private int page;
    private int left;
    private int contentWidth;
    private int columnWidth;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("inventory_slots.config.title"));

        this.parent = parent;
    }

    @Override
    protected void init() {
        contentWidth = Math.min(440, width - 20);
        left = (width - contentWidth) / 2;
        columnWidth = (contentWidth - 6) / 2;

        String[] tabs = {"general", "appearance", "types", "exclusions"};
        int tabWidth = (contentWidth - 9) / 4;

        for (int i = 0; i < tabs.length; i++) {
            int selected = i;

            ButtonWidget button = button(Text.translatable("inventory_slots.tab." + tabs[i]),
                left + i * (tabWidth + 3), 34, tabWidth, () -> {
                    tab = selected;
                    page = 0;
                    clearAndInit();
                });
            button.active = tab != i;
        }

        switch (tab) {
            case 0 -> general();
            case 1 -> appearance();
            case 2 -> types();
            case 3 -> exclusions();
            default -> throw new IllegalStateException("Unknown tab: " + tab);
        }

        int footerWidth = (contentWidth - 12) / 3;

        button(Text.translatable("inventory_slots.reset"), left, height - 26, footerWidth, () -> {
            config.reset();
            page = 0;
            clearAndInit();
        }).setTooltip(Tooltip.of(Text.translatable("inventory_slots.reset.tooltip")));

        button(Text.translatable("inventory_slots.keybinds"), left + footerWidth + 6,
            height - 26, footerWidth,
            () -> client.setScreen(new KeybindsScreen(this, client.options)));

        button(Text.translatable("gui.done"), left + 2 * (footerWidth + 6),
            height - 26, footerWidth, this::close);
    }

    private void general() {
        toggle("enabled", 0, 0, () -> config.enabled, v -> config.enabled = v);

        button(Text.translatable("inventory_slots.containers_only"), right(), 64, columnWidth, () -> {
            config.containersOnly();
            clearAndInit();
        }).setTooltip(Tooltip.of(Text.translatable("inventory_slots.containers_only.tooltip")));

        toggle("container_slots", 1, 0, () -> config.containerSlots, v -> config.containerSlots = v);
        toggle("inventory_screen", 1, 1, () -> config.inventoryScreen, v -> config.inventoryScreen = v);
        toggle("player_inventory", 2, 0, () -> config.playerInventoryInContainers,
            v -> config.playerInventoryInContainers = v);
        toggle("player_hotbar", 2, 1, () -> config.playerHotbarInContainers, v -> config.playerHotbarInContainers = v);
        toggle("creative", 3, 0, () -> config.creativeInventory, v -> config.creativeInventory = v);
        toggle("hotbar", 3, 1, () -> config.hotbarNumbers, v -> {
            config.hotbarNumbers = v;
            if (!v) {
                config.hotbarOnly = false;
            }
            clearAndInit();
        });
        toggle("widgets", 4, 0, () -> config.widgetNumbers, v -> config.widgetNumbers = v);
        toggle("hotbar_only", 4, 1, () -> config.hotbarOnly, v -> {
            config.hotbarOnly = v;
            if (v) {
                config.hotbarNumbers = true;
            }
            clearAndInit();
        });
    }

    private void appearance() {
        button(option("anchor", Text.translatable(config.textAnchor.translationKey())),
            left, 64, columnWidth, () -> {
                var anchors = Config.TextAnchor.values();
                config.textAnchor = anchors[(config.textAnchor.ordinal() + 1) % anchors.length];
                clearAndInit();
            });

        button(option("color", Text.translatable(config.colorTranslationKey())),
            right(), 64, columnWidth, () -> {
                int index = -1;

                for (int i = 0; i < Config.COLOR_PALETTE.length; i++) {
                    if (Config.COLOR_PALETTE[i] == config.textColor) {
                        index = i;
                    }
                }

                config.textColor = Config.COLOR_PALETTE[(index + 1) % Config.COLOR_PALETTE.length];
                clearAndInit();
            });

        toggle("shadow", 1, 0, () -> config.textShadow, v -> config.textShadow = v);

        addDrawableChild(new SliderWidget(right(), 88, columnWidth, 20,
            option("scale", Text.literal(String.format(Locale.ROOT, "%.2f", config.textScale))),
            (config.textScale - 0.1) / 0.9) {
            @Override
            protected void updateMessage() {
                setMessage(option("scale", Text.literal(String.format(Locale.ROOT, "%.2f", 0.1 + value * 0.9))));
            }

            @Override
            protected void applyValue() {
                config.textScale = (float) (0.1 + value * 0.9);
            }
        });
    }

    private void types() {
        TreeSet<String> ids = new TreeSet<>();

        Registries.SCREEN_HANDLER.getIds().forEach(id -> ids.add(id.toString()));
        ids.add("net.minecraft.screen.HorseScreenHandler");
        ids.addAll(config.disabledMenuTypes);

        if (parent instanceof HandledScreen<?> inventory) {
            ids.add(SlotVisibility.menuType(inventory));
        }

        if (!config.disabledMenuTypes.contains("inventory_slots:player")) {
            ids.remove("inventory_slots:player");
        }

        if (!config.disabledMenuTypes.contains("inventory_slots:creative")) {
            ids.remove("inventory_slots:creative");
        }

        List<String> entries = new ArrayList<>(ids);
        int pageSize = rows(64) * 2;

        paginate(entries.size(), pageSize);

        for (int i = page * pageSize; i < Math.min(entries.size(), (page + 1) * pageSize); i++) {
            String id = entries.get(i);
            int position = i - page * pageSize;
            Text name = menuName(id);
            int entryX = left;

            if (position % 2 != 0) {
                entryX = right();
            }

            ButtonWidget entry = button(status(name, !config.disabledMenuTypes.contains(id)),
                entryX, 64 + position / 2 * 24, columnWidth, () -> {
                    config.toggleMenuType(id);
                    clearAndInit();
                });

            entry.setTooltip(Tooltip.of(name.copy().append("\n" + id + "\n")
                .append(Text.translatable("inventory_slots.types.tooltip"))));
        }
    }

    private void exclusions() {
        ButtonWidget current = button(Text.translatable("inventory_slots.current_screen"), left, 64, contentWidth,
            () -> {
                if (parent instanceof HandledScreen<?> inventory) {
                    config.toggleScreen(SlotVisibility.menuType(inventory), parent.getTitle().getString());
                    clearAndInit();
                }
            });

        if (parent instanceof HandledScreen<?> inventory) {
            boolean allowed = !config.excludedScreens.contains(new Config.ScreenRule(
                SlotVisibility.menuType(inventory), parent.getTitle().getString()));
            current.setMessage(status(parent.getTitle(), allowed));
            current.setTooltip(Tooltip.of(Text.translatable("inventory_slots.current_screen.tooltip")));
        } else {
            current.active = false;
            current.setTooltip(Tooltip.of(Text.translatable("inventory_slots.open_inventory")));
        }

        List<Config.ScreenRule> entries = new ArrayList<>(config.excludedScreens);
        int pageSize = rows(92);

        paginate(entries.size(), pageSize);

        for (int i = page * pageSize; i < Math.min(entries.size(), (page + 1) * pageSize); i++) {
            Config.ScreenRule rule = entries.get(i);
            button(Text.translatable("inventory_slots.remove_exclusion", rule.title()),
                left, 92 + (i - page * pageSize) * 24, contentWidth, () -> {
                    config.excludedScreens.remove(rule);
                    clearAndInit();
                }).setTooltip(Tooltip.of(Text.literal(rule.menuType() + "\n" + rule.title())));
        }
    }

    private int rows(int startY) {
        return Math.max(1, (height - 60 - startY) / 24);
    }

    private int right() {
        return left + columnWidth + 6;
    }

    private void paginate(int count, int pageSize) {
        int pages = Math.max(1, (count + pageSize - 1) / pageSize);
        page = Math.min(page, pages - 1);

        button(Text.literal("<"), left, height - 54, 40, () -> {
            page--;
            clearAndInit();
        }).active = page > 0;

        button(Text.literal(">"), left + contentWidth - 40, height - 54, 40, () -> {
            page++;
            clearAndInit();
        }).active = page + 1 < pages;
    }

    private void toggle(String key, int row, int column, BooleanSupplier getter, Consumer<Boolean> setter) {
        Text name = Text.translatable("inventory_slots.option." + key);
        int entryX = left;

        if (column != 0) {
            entryX = right();
        }

        ButtonWidget entry = button(status(name, getter.getAsBoolean()), entryX,
            64 + row * 24, columnWidth, () -> {
                setter.accept(!getter.getAsBoolean());
                clearAndInit();
            });
        entry.setTooltip(Tooltip.of(Text.translatable("inventory_slots.option." + key + ".tooltip")));
    }

    private ButtonWidget button(Text label, int x, int y, int buttonWidth, Runnable action) {
        return addDrawableChild(ButtonWidget.builder(label, button -> action.run()).dimensions(x, y, buttonWidth, 20).build());
    }

    private static Text option(String key, Text value) {
        return Text.translatable("inventory_slots.option." + key).append(": ").append(value);
    }

    private static Text status(Text label, boolean enabled) {
        return label.copy().append(": ").append(InventorySlotsClient.onOff(enabled));
    }

    private static Text menuName(String id) {
        String key = "inventory_slots.menu." + id.replace(':', '.');
        return Text.translatableWithFallback(key, id);
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFFFF);

        if (tab == 1) {
            int y = 134;
            graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable("inventory_slots.preview"), width / 2, y - 14,
                0xFFFFFFFF);

            for (int i = 0; i < 6; i++) {
                int x = width / 2 - 29 + (i % 3) * 20;
                int slotY = y + i / 3 * 20;

                graphics.fill(x, slotY, x + 18, slotY + 18, 0xFF8B8B8B);
                graphics.fill(x + 1, slotY + 1, x + 17, slotY + 17, 0xFF373737);
                SlotLabelRenderer.draw(graphics, textRenderer, String.valueOf(i), x + 1, slotY + 1, 16, 16);
            }
        }

        if (tab == 2 || tab == 3) {
            graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable("inventory_slots.page", page + 1),
                width / 2, height - 48, 0xFFAAAAAA);
            if (tab == 3 && config.excludedScreens.isEmpty()) {
                graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable("inventory_slots.no_exclusions"),
                    width / 2, 100, 0xFFAAAAAA);
            }
        }
    }

    @Override
    public void removed() {
        config.save();
    }

    @Override
    public void close() {
        config.save();
        client.setScreen(parent);
    }
}
