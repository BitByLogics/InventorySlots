package net.bitbylogic.inventoryslots.client;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class SlotVisibility {
    private SlotVisibility() {
    }

    public static Config.ScreenKind kind(AbstractContainerScreen<?> screen) {
        if (screen instanceof CreativeModeInventoryScreen) {
            return Config.ScreenKind.CREATIVE;
        }
        if (screen instanceof InventoryScreen) {
            return Config.ScreenKind.INVENTORY;
        }
        return Config.ScreenKind.CONTAINER;
    }

    public static String menuType(AbstractContainerScreen<?> screen) {
        if (screen instanceof CreativeModeInventoryScreen) {
            return "inventory_slots:creative";
        }
        if (screen instanceof InventoryScreen) {
            return "inventory_slots:player";
        }
        try {
            var id = BuiltInRegistries.MENU.getKey(screen.getMenu().getType());
            if (id != null) {
                return id.toString();
            }
        } catch (UnsupportedOperationException ignored) {
            // Horse inventories and some modded menus do not have a registered MenuType.
        }
        return screen.getMenu().getClass().getName();
    }

    public static boolean shouldShow(AbstractContainerScreen<?> screen, Slot slot) {
        boolean playerSlot = slot.container instanceof Inventory;
        return slot.isActive() && Config.INSTANCE.shouldShowSlot(kind(screen), menuType(screen),
            screen.getTitle().getString(), playerSlot,
            playerSlot && Inventory.isHotbarSlot(slot.getContainerSlot()));
    }
}
