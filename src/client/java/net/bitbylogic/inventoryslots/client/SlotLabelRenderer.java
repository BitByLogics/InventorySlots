package net.bitbylogic.inventoryslots.client;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

public class SlotLabelRenderer {

    public static void draw(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int width, int height) {
        float scale = Config.INSTANCE.textScale;

        float textWidth = textRenderer.getWidth(text) * scale;
        float textHeight = textRenderer.fontHeight * scale;

        float offsetX = Config.INSTANCE.textAnchor.getOffsetX(textWidth, width);
        float offsetY = Config.INSTANCE.textAnchor.getOffsetY(textHeight, height);

        Matrix3x2fStack matrices = context.getMatrices();

        matrices.pushMatrix();

        matrices.translate(x + offsetX, y + offsetY);
        matrices.scale(scale, scale);

        context.drawText(textRenderer, text, 0, 0, Config.INSTANCE.textColor, Config.INSTANCE.textShadow);

        matrices.popMatrix();
    }

}
