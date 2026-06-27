package net.bitbylogic.inventoryslots.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.bitbylogic.inventoryslots.client.config.ConfigScreen;
import net.bitbylogic.inventoryslots.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class InventorySlotsClient implements ClientModInitializer {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("category.inventory_slots.general"));

    public static final KeyMapping OPEN_CONFIG = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.inventory_slots.config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_END,
            CATEGORY
    ));

    public static final KeyMapping TOGGLE_SLOTS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.inventory_slots.toggle_slots",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_HOME,
            CATEGORY
    ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_SLOTS.consumeClick()) {
                Config.INSTANCE.enabled = !Config.INSTANCE.enabled;
                Config.INSTANCE.save();

                MutableComponent message = Component.literal("Slots")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY));

                Component statusText = Config.INSTANCE.enabled
                        ? Component.literal("Enabled").withStyle(ChatFormatting.GREEN)
                        : Component.literal("Disabled").withStyle(ChatFormatting.RED);

                message.append(statusText);

                client.gui.setOverlayMessage(message, false);
            }

            while (OPEN_CONFIG.consumeClick()) {
                client.setScreen(new ConfigScreen(client.screen));
            }
        });

        Config.INSTANCE.load();
    }

}