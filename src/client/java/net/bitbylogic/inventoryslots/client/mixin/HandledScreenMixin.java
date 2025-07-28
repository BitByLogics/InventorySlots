package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    protected HandledScreenMixin() {
        super(null);
    }

    @Shadow protected int x;
    @Shadow protected int y;

    @Final
    @Shadow protected T handler;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!Config.INSTANCE.enabled || client.currentScreen instanceof CreativeInventoryScreen) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        List<Slot> slots = handler.slots;
        float scale = Config.INSTANCE.textScale;

        MatrixStack matrices = context.getMatrices();

        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);

            if (!slot.isEnabled()) {
                continue;
            }

            int slotX = x + slot.x;
            int slotY = y + slot.y;

            String text = String.valueOf(i);
            float textWidth = textRenderer.getWidth(text) * scale;
            float textHeight = textRenderer.fontHeight * scale;

            float offsetX = Config.INSTANCE.textAnchor.getOffsetX(textWidth, 16);
            float offsetY = Config.INSTANCE.textAnchor.getOffsetY(textHeight, 16);

            matrices.push();
            matrices.translate(slotX + offsetX, slotY + offsetY, 0);
            matrices.scale(scale, scale, 1);

            if (Config.INSTANCE.textShadow) {
                context.drawTextWithShadow(
                        textRenderer,
                        text,
                        0,
                        0,
                        Config.INSTANCE.textColor
                );

                matrices.pop();

                continue;
            }

            context.drawText(
                    textRenderer,
                    text,
                    0,
                    0,
                    Config.INSTANCE.textColor,
                    false
            );

            matrices.pop();
        }
    }

}
