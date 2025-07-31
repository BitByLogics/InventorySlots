package net.bitbylogic.inventoryslots.client;

import net.bitbylogic.inventoryslots.client.config.ConfigScreen;
import net.bitbylogic.inventoryslots.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class InventorySlotsClient implements ClientModInitializer {

    public static final KeyBinding OPEN_CONFIG = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.inventory_slots.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_END,
            "category.inventory_slots.general"
    ));

    public static final KeyBinding TOGGLE_SLOTS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.inventory_slots.toggle_slots",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_HOME,
            "category.inventory_slots.general"
    ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_SLOTS.wasPressed()) {
                Config.INSTANCE.enabled = !Config.INSTANCE.enabled;
                Config.INSTANCE.save();

                MutableText message = Text.literal("Slots")
                        .formatted(Formatting.GRAY)
                        .append(Text.literal(" • ").formatted(Formatting.DARK_GRAY));

                Text statusText = Config.INSTANCE.enabled
                        ? Text.literal("Enabled").formatted(Formatting.GREEN)
                        : Text.literal("Disabled").formatted(Formatting.RED);

                message.append(statusText);

                client.inGameHud.setOverlayMessage(message, false);
            }

            while (OPEN_CONFIG.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen));
            }
        });

        Config.INSTANCE.load();
    }

}
