package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.client.SlotLabelRenderer;
import net.bitbylogic.inventoryslots.client.SlotVisibility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    protected HandledScreenMixin() {
        super(null);
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void onDrawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        if (!SlotVisibility.shouldShow(screen, slot)) {
            return;
        }

        SlotLabelRenderer.draw(context, MinecraftClient.getInstance().textRenderer,
                String.valueOf(SlotVisibility.slotNumber(screen, slot)), slot.x, slot.y, 16, 16);
    }

}
