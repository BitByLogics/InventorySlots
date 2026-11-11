package net.bitbylogic.inventoryslots.client;

import net.minecraft.client.util.InputUtil;
import net.bitbylogic.inventoryslots.client.config.ConfigScreen;
import net.bitbylogic.inventoryslots.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.util.Formatting;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class InventorySlotsClient implements ClientModInitializer {

    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
        Identifier.of("inventory_slots", "general"));

    public enum Action {
        CONFIG("config", GLFW.GLFW_KEY_END),
        TOGGLE("toggle_slots", GLFW.GLFW_KEY_HOME),
        CONTAINERS("toggle_containers"),
        PLAYER("toggle_player"),
        PLAYER_HOTBAR("toggle_player_hotbar"),
        INVENTORY("toggle_inventory"),
        CREATIVE("toggle_creative"),
        HUD("toggle_hotbar"),
        WIDGETS("toggle_widgets"),
        SCREEN("toggle_screen"),
        TYPE("toggle_type");

        public final KeyBinding key;
        private boolean down;

        Action(String name) {
            this(name, InputUtil.UNKNOWN_KEY.getCode());
        }

        Action(String name, int defaultKey) {
            key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.inventory_slots." + name, InputUtil.Type.KEYSYM, defaultKey, CATEGORY));
        }
    }

    public static final KeyBinding OPEN_CONFIG = Action.CONFIG.key;
    public static final KeyBinding TOGGLE_SLOTS = Action.TOGGLE.key;

    private final Set<InputUtil.Key> heldScreenKeys = new HashSet<>();

    @Override
    public void onInitializeClient() {
        Config.INSTANCE.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Action action : Action.values()) {
                boolean clicked = false;

                while (action.key.wasPressed()) {
                    clicked = true;
                }

                boolean pressed = clicked && !action.down;
                action.down = action.key.isPressed();

                if (client.currentScreen != null || !pressed) {
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
                    if (action.key.isUnbound() || !action.key.matchesKey(event) || !applicable(action, current)) {
                        continue;
                    }

                    if (heldScreenKeys.add(InputUtil.fromKeyCode(event))) {
                        run(action, client, current);
                    }

                    return false;
                }

                return true;
            });

            ScreenKeyboardEvents.allowKeyRelease(screen).register((current, event) -> {
                heldScreenKeys.remove(InputUtil.fromKeyCode(event));
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
        return screen instanceof HandledScreen<?> || screen instanceof TitleScreen || screen instanceof GameMenuScreen;
    }

    private static boolean typing(Screen screen) {
        return screen.getFocused() instanceof TextFieldWidget edit && edit.isActive();
    }

    private static boolean applicable(Action action, Screen screen) {
        return screen instanceof HandledScreen<?> || action == Action.CONFIG;
    }

    private static void run(Action action, MinecraftClient client, Screen screen) {
        Config config = Config.INSTANCE;

        if (action == Action.CONFIG) {
            client.setScreen(new ConfigScreen(screen));
            return;
        }

        Text label;
        boolean enabled;
        if (action == Action.SCREEN || action == Action.TYPE) {
            if (!(screen instanceof HandledScreen<?> inventory)) {
                message(client, Text.translatable("inventory_slots.open_inventory"));
                return;
            }
            String type = SlotVisibility.menuType(inventory);

            if (action == Action.SCREEN) {
                enabled = config.toggleScreen(type, screen.getTitle().getString());
                label = screen.getTitle();
            } else {
                enabled = config.toggleMenuType(type);
                label = Text.literal(type);
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
            label = Text.translatable(action.key.getId());

            if (action == Action.HUD && !config.hotbarNumbers) {
                config.hotbarOnly = false;
            }
        }

        config.save();
        message(client, label.copy().formatted(Formatting.GRAY)
            .append(Text.literal(" • ").formatted(Formatting.DARK_GRAY))
            .append(onOff(enabled)));
    }

    public static Text onOff(boolean enabled) {
        if (enabled) {
            return Text.translatable("options.on").formatted(Formatting.GREEN);
        }

        return Text.translatable("options.off").formatted(Formatting.RED);
    }

    private static void message(MinecraftClient client, Text text) {
        if (client.player != null) {
            client.inGameHud.setOverlayMessage(text, false);
        }
    }
}
