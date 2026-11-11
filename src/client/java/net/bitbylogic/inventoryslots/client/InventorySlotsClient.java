package net.bitbylogic.inventoryslots.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.bitbylogic.inventoryslots.client.config.ConfigScreen;
import net.bitbylogic.inventoryslots.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public class InventorySlotsClient implements ClientModInitializer {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.parse("inventory_slots:general"));

    public enum Action {
        CONFIG("config", InputConstants.KEY_END),
        TOGGLE("toggle_slots", InputConstants.KEY_HOME),
        CONTAINERS("toggle_containers"),
        PLAYER("toggle_player"),
        PLAYER_HOTBAR("toggle_player_hotbar"),
        INVENTORY("toggle_inventory"),
        CREATIVE("toggle_creative"),
        HUD("toggle_hotbar"),
        WIDGETS("toggle_widgets"),
        SCREEN("toggle_screen"),
        TYPE("toggle_type");

        public final KeyMapping key;
        private boolean down;

        Action(String name) {
            this(name, InputConstants.UNKNOWN.getValue());
        }

        Action(String name, int defaultKey) {
            key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.inventory_slots." + name, InputConstants.Type.KEYBOARD, defaultKey, CATEGORY));
        }
    }

    public static final KeyMapping OPEN_CONFIG = Action.CONFIG.key;
    public static final KeyMapping TOGGLE_SLOTS = Action.TOGGLE.key;

    private final Set<InputConstants.Key> heldScreenKeys = new HashSet<>();

    @Override
    public void onInitializeClient() {
        Config.INSTANCE.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Action action : Action.values()) {
                boolean clicked = false;

                while (action.key.consumeClick()) {
                    clicked = true;
                }

                boolean pressed = clicked && !action.down;
                action.down = action.key.isDown();

                if (client.gui.screen() != null || !pressed) {
                    continue;
                }

                run(action, client, null);
            }
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (!supportsHotkeys(screen)) {
                return;
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register((current, event) -> {
                if (typing(current)) {
                    return true;
                }

                for (Action action : Action.values()) {
                    if (action.key.isUnbound() || !action.key.matches(event) || !applicable(action, current)) {
                        continue;
                    }

                    if (heldScreenKeys.add(InputConstants.getKey(event))) {
                        run(action, client, current);
                    }

                    return false;
                }

                return true;
            });

            ScreenKeyboardEvents.allowKeyRelease(screen).register((current, event) -> {
                heldScreenKeys.remove(InputConstants.getKey(event));
                return true;
            });

            ScreenEvents.remove(screen).register(current -> heldScreenKeys.clear());

            ScreenMouseEvents.allowMouseClick(screen).register((current, event) -> {
                if (typing(current)) {
                    return true;
                }

                for (Action action : Action.values()) {
                    if (action.key.isUnbound() || !action.key.matchesMouse(event) || !applicable(action, current)) {
                        continue;
                    }

                    run(action, client, current);
                    return false;
                }

                return true;
            });
        });
    }

    private static boolean supportsHotkeys(Screen screen) {
        return screen instanceof AbstractContainerScreen<?> || screen instanceof TitleScreen || screen instanceof PauseScreen;
    }

    private static boolean typing(Screen screen) {
        return screen.getFocused() instanceof EditBox edit && edit.canConsumeInput();
    }

    private static boolean applicable(Action action, Screen screen) {
        return screen instanceof AbstractContainerScreen<?> || action == Action.CONFIG;
    }

    private static void run(Action action, Minecraft client, Screen screen) {
        Config config = Config.INSTANCE;

        if (action == Action.CONFIG) {
            client.setScreenAndShow(new ConfigScreen(screen));
            return;
        }

        Component label;
        boolean enabled;
        if (action == Action.SCREEN || action == Action.TYPE) {
            if (!(screen instanceof AbstractContainerScreen<?> inventory)) {
                message(client, Component.translatable("inventory_slots.open_inventory"));
                return;
            }
            String type = SlotVisibility.menuType(inventory);

            if (action == Action.SCREEN) {
                enabled = config.toggleScreen(type, screen.getTitle().getString());
                label = screen.getTitle();
            } else {
                enabled = config.toggleMenuType(type);
                label = Component.literal(type);
            }
        } else {
            enabled = switch (action) {
                case TOGGLE -> config.enabled = !config.enabled;
                case CONTAINERS -> config.containerSlots = !config.containerSlots;
                case PLAYER -> config.playerInventoryInContainers = !config.playerInventoryInContainers;
                case PLAYER_HOTBAR -> config.playerHotbarInContainers = !config.playerHotbarInContainers;
                case INVENTORY -> config.inventoryScreen = !config.inventoryScreen;
                case CREATIVE -> config.creativeInventory = !config.creativeInventory;
                case HUD -> config.hotbarNumbers = !config.hotbarNumbers;
                case WIDGETS -> config.widgetNumbers = !config.widgetNumbers;
                default -> throw new IllegalStateException("Unexpected action: " + action);
            };
            label = Component.translatable(action.key.getName());

            if (action == Action.HUD && !config.hotbarNumbers) {
                config.hotbarOnly = false;
            }
        }

        config.save();
        message(client, label.copy().withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY))
            .append(onOff(enabled)));
    }

    public static Component onOff(boolean enabled) {
        if (enabled) {
            return Component.translatable("options.on").withStyle(ChatFormatting.GREEN);
        }

        return Component.translatable("options.off").withStyle(ChatFormatting.RED);
    }

    private static void message(Minecraft client, Component text) {
        if (client.player != null) {
            client.player.sendOverlayMessage(text);
        }
    }
}
