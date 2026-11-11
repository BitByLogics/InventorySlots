package net.bitbylogic.inventoryslots.client;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class SlotVisibility {

    private static AbstractContainerScreen<?> cachedScreen;
    private static String cachedType;
    private static String cachedTitle;

    private SlotVisibility() {
    }

    private static void cache(AbstractContainerScreen<?> screen) {
        if (cachedScreen == screen) {
            return;
        }

        cachedScreen = screen;
        cachedType = menuType(screen);
        cachedTitle = screen.getTitle().getString();
    }

    public static boolean screenAllowed(AbstractContainerScreen<?> screen) {
        cache(screen);

        return Config.INSTANCE.isScreenAllowed(cachedType, cachedTitle);
    }

    public static int slotNumber(AbstractContainerScreen<?> screen, Slot slot) {
        return screen.getMenu().slots.indexOf(slot);
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
        }
        return screen.getMenu().getClass().getName();
    }

    public static boolean shouldShow(AbstractContainerScreen<?> screen, Slot slot) {
        cache(screen);

        boolean playerSlot = slot.container instanceof Inventory;
        boolean hotbarSlot = playerSlot && Inventory.isHotbarSlot(slot.getContainerSlot());

        return slot.isActive() && Config.INSTANCE.shouldShowSlot(kind(screen), cachedType, cachedTitle,
            playerSlot, hotbarSlot);
    }
}
