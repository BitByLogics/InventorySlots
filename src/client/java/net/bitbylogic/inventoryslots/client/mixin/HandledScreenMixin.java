package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.client.SlotLabelRenderer;
import net.bitbylogic.inventoryslots.client.SlotVisibility;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen {

    protected HandledScreenMixin() {
        super(Component.empty());
    }

    @Final
    @Shadow protected T menu;

    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void onExtractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        if (!SlotVisibility.shouldShow(screen, slot)) {
            return;
        }

        SlotLabelRenderer.draw(graphics, getFont(), String.valueOf(SlotVisibility.slotNumber(screen, slot)), slot.x, slot.y, 16, 16);
    }

}
