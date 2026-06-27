package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract Font getFont();

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    private void renderHotbarNumbers(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!Config.INSTANCE.enabled || !Config.INSTANCE.hotbarNumbers || minecraft.player == null) {
            return;
        }

        Font font = getFont();
        float scale = Config.INSTANCE.textScale;

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        for (int i = 0; i < 9; i++) {
            int slotX = width / 2 - 88 + i * 20;
            int slotY = height - 19;

            String text = String.valueOf(i);

            float textWidth = font.width(text);
            float textHeight = font.lineHeight;

            float offsetX = Config.INSTANCE.textAnchor.getOffsetX(textWidth * scale, 16);
            float offsetY = Config.INSTANCE.textAnchor.getOffsetY(textHeight * scale, 16);

            var pose = graphics.pose();

            pose.pushMatrix();

            pose.translate(slotX + offsetX, slotY + offsetY);
            pose.scale(scale, scale);

            graphics.text(
                    font,
                    text,
                    0,
                    0,
                    Config.INSTANCE.textColor,
                    Config.INSTANCE.textShadow
            );

            pose.popMatrix();
        }
    }
}
