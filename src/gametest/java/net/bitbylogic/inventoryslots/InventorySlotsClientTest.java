package net.bitbylogic.inventoryslots;

import com.mojang.blaze3d.platform.InputConstants;
import net.bitbylogic.inventoryslots.client.InventorySlotsClient;
import net.bitbylogic.inventoryslots.client.SlotVisibility;
import net.bitbylogic.inventoryslots.client.config.ConfigScreen;
import net.bitbylogic.inventoryslots.config.Config;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.level.GameType;

@SuppressWarnings("UnstableApiUsage")
public class InventorySlotsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.runOnClient(client -> Config.INSTANCE.containersOnly());
            context.setScreen(() -> {
                var client = net.minecraft.client.Minecraft.getInstance();
                var menu = ChestMenu.sixRows(1, client.player.getInventory());
                client.player.containerMenu = menu;
                return new ContainerScreen(menu, client.player.getInventory(), Component.literal("Test Chest"));
            });
            assertVisibleSlots(context, 54);
            context.takeScreenshot("container-only-chest");

            context.runOnClient(client -> {
                Config.INSTANCE.playerInventoryInContainers = true;
                Config.INSTANCE.playerHotbarInContainers = false;
            });
            assertVisibleSlots(context, 81);
            context.runOnClient(client -> {
                Config.INSTANCE.playerInventoryInContainers = false;
                Config.INSTANCE.playerHotbarInContainers = true;
            });
            assertVisibleSlots(context, 63);

            // A freely rebound action must work inside an inventory, once per press.
            context.runOnClient(client -> {
                InventorySlotsClient.Action.SCREEN.key.setKey(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_F8));
                KeyMapping.resetMapping();
            });
            context.getInput().pressKey(InputConstants.KEY_F8);
            assertVisibleSlots(context, 0);
            context.getInput().pressKey(InputConstants.KEY_F8);
            assertVisibleSlots(context, 63);

            context.getInput().pressKey(InventorySlotsClient.OPEN_CONFIG);
            context.waitForScreen(ConfigScreen.class);
            context.clickScreenButton("inventory_slots.containers_only");
            context.takeScreenshot("settings-visibility");
            context.clickScreenButton("inventory_slots.tab.appearance");
            context.takeScreenshot("settings-appearance");
            context.clickScreenButton("inventory_slots.tab.types");
            context.takeScreenshot("settings-types");
            context.clickScreenButton("inventory_slots.tab.exclusions");
            context.takeScreenshot("settings-exclusions");
            context.clickScreenButton("inventory_slots.keybinds");
            context.waitForScreen(KeyBindsScreen.class);
            context.getInput().pressKey(InputConstants.KEY_ESCAPE);
            context.waitForScreen(ConfigScreen.class);
            context.getInput().pressKey(InputConstants.KEY_ESCAPE);
            context.waitForScreen(ContainerScreen.class);
            assertVisibleSlots(context, 54);

            context.setScreen(() -> {
                var client = net.minecraft.client.Minecraft.getInstance();
                var menu = new FurnaceMenu(2, client.player.getInventory());
                client.player.containerMenu = menu;
                return new FurnaceScreen(menu, client.player.getInventory(), Component.literal("Test Furnace"));
            });
            assertVisibleSlots(context, 3);
            context.takeScreenshot("container-only-furnace");
            context.runOnClient(client -> Config.INSTANCE.toggleMenuType("minecraft:furnace"));
            assertVisibleSlots(context, 0);

            context.setScreen(() -> new InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
            assertVisibleSlots(context, 0);
            context.takeScreenshot("hidden-player-inventory");
            context.runOnClient(client -> Config.INSTANCE.inventoryScreen = true);
            assertVisibleSlots(context, 46);

            context.setScreen(() -> {
                var client = net.minecraft.client.Minecraft.getInstance();
                client.gameMode.setLocalMode(GameType.CREATIVE);
                return new CreativeModeInventoryScreen(client.player, client.player.level().enabledFeatures(), false);
            });
            assertVisibleSlots(context, 0);
            context.takeScreenshot("hidden-creative-inventory");
            context.runOnClient(client -> Config.INSTANCE.creativeInventory = true);
            context.runOnClient(client -> {
                var screen = (AbstractContainerScreen<?>) client.gui.screen();
                check(screen.getMenu().slots.stream().anyMatch(slot -> SlotVisibility.shouldShow(screen, slot)),
                        "Creative inventory did not enable");
            });

            context.setScreen(() -> null);
            context.runOnClient(client -> Config.INSTANCE.enabled = true);
            context.getInput().pressKey(InventorySlotsClient.TOGGLE_SLOTS);
            context.runOnClient(client -> check(!Config.INSTANCE.enabled, "World hotkey did not disable numbers"));
            context.getInput().pressKey(InventorySlotsClient.TOGGLE_SLOTS);
            context.runOnClient(client -> check(Config.INSTANCE.enabled, "World hotkey did not reenable numbers"));
        }
    }

    private static void assertVisibleSlots(ClientGameTestContext context, long expected) {
        context.runOnClient(client -> {
            var screen = (AbstractContainerScreen<?>) client.gui.screen();
            long actual = screen.getMenu().slots.stream().filter(slot -> SlotVisibility.shouldShow(screen, slot)).count();
            check(actual == expected, screen.getClass().getSimpleName() + ": expected " + expected + " slots, got " + actual);
        });
        context.waitTick();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
