package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderHotbarNumbers(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!Config.INSTANCE.enabled || !Config.INSTANCE.hotbarNumbers || client.player == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        float scale = Config.INSTANCE.textScale;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        for (int i = 0; i < 9; i++) {
            int slotX = width / 2 - 88 + i * 20;
            int slotY = height - 19;

            String text = String.valueOf(i + 1);
            float textWidth = textRenderer.getWidth(text) * scale;
            float textHeight = textRenderer.fontHeight * scale;

            float offsetX = Config.INSTANCE.textAnchor.getOffsetX(textWidth, 16);
            float offsetY = Config.INSTANCE.textAnchor.getOffsetY(textHeight, 16);

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(slotX + offsetX, slotY + offsetY);
            context.getMatrices().scale(scale, scale);

            if (Config.INSTANCE.textShadow) {
                context.drawTextWithShadow(textRenderer, text, 0, 0, Config.INSTANCE.textColor);
            } else {
                context.drawText(textRenderer, text, 0, 0, Config.INSTANCE.textColor, false);
            }

            context.getMatrices().popMatrix();
        }
    }
}
