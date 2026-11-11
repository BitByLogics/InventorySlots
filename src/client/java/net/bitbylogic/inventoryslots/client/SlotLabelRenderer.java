package net.bitbylogic.inventoryslots.client;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SlotLabelRenderer {

    public static void draw(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int width, int height) {
        float scale = Config.INSTANCE.textScale;

        float textWidth = font.width(text) * scale;
        float textHeight = font.lineHeight * scale;

        float offsetX = Config.INSTANCE.textAnchor.getOffsetX(textWidth, width);
        float offsetY = Config.INSTANCE.textAnchor.getOffsetY(textHeight, height);

        var pose = graphics.pose();

        pose.pushMatrix();

        pose.translate(x + offsetX, y + offsetY);
        pose.scale(scale, scale);

        graphics.text(font, text, 0, 0, Config.INSTANCE.textColor, Config.INSTANCE.textShadow);

        pose.popMatrix();
    }

}
