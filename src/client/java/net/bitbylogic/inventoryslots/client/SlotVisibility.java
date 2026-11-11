package net.bitbylogic.inventoryslots.client;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;

public final class SlotVisibility {

    private static HandledScreen<?> cachedScreen;
    private static String cachedType;
    private static String cachedTitle;

    private SlotVisibility() {
    }

    private static void cache(HandledScreen<?> screen) {
        if (cachedScreen == screen) {
            return;
        }

        cachedScreen = screen;
        cachedType = menuType(screen);
        cachedTitle = screen.getTitle().getString();
    }

    public static boolean screenAllowed(HandledScreen<?> screen) {
        cache(screen);

        return Config.INSTANCE.isScreenAllowed(cachedType, cachedTitle);
    }

    public static int slotNumber(HandledScreen<?> screen, Slot slot) {
        return screen.getScreenHandler().slots.indexOf(slot);
    }

    public static Config.ScreenKind kind(HandledScreen<?> screen) {
        if (screen instanceof CreativeInventoryScreen) {
            return Config.ScreenKind.CREATIVE;
        }
        if (screen instanceof InventoryScreen) {
            return Config.ScreenKind.INVENTORY;
        }
        return Config.ScreenKind.CONTAINER;
    }

    public static String menuType(HandledScreen<?> screen) {
        if (screen instanceof CreativeInventoryScreen) {
            return "inventory_slots:creative";
        }
        if (screen instanceof InventoryScreen) {
            return "inventory_slots:player";
        }
        try {
            var id = Registries.SCREEN_HANDLER.getId(screen.getScreenHandler().getType());
            if (id != null) {
                return id.toString();
            }
        } catch (UnsupportedOperationException ignored) {
        }
        return screen.getScreenHandler().getClass().getName();
    }

    public static boolean shouldShow(HandledScreen<?> screen, Slot slot) {
        cache(screen);

        boolean playerSlot = slot.inventory instanceof PlayerInventory;
        boolean hotbarSlot = playerSlot && PlayerInventory.isValidHotbarIndex(slot.getIndex());

        return slot.isEnabled() && Config.INSTANCE.shouldShowSlot(kind(screen), cachedType, cachedTitle,
            playerSlot, hotbarSlot);
    }
}
