
package net.bitbylogic.inventoryslots.client.config;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

public class ConfigScreen extends Screen {

    private final Screen parent;

    private SliderWidget scaleSlider;
    private ButtonWidget anchorButton;
    private ButtonWidget colorButton;
    private ButtonWidget shadowButton;

    private int anchorIndex;
    private int colorIndex = 0;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Inventory Slots Config"));

        this.parent = parent;

        anchorIndex = Config.INSTANCE.textAnchor.ordinal();

        for (int i = 0; i < Config.COLOR_PALETTE.length; i++) {
            if (Config.COLOR_PALETTE[i] == Config.INSTANCE.textColor) {
                colorIndex = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int contentWidth = Math.min(300, width - 40);
        int contentX = (width - contentWidth) / 2;

        int buttonHeight = 20;
        int spacing = 8;

        int controlBlockHeight = buttonHeight * 4 + spacing * 3;
        int previewHeight = 2 * 18 + spacing;

        int topMargin = 60;
        int availableHeight = height - topMargin - 30;
        int centerY = topMargin + (availableHeight / 2);
        int startY = centerY - (controlBlockHeight + spacing + 10 + previewHeight) / 2;

        scaleSlider = new SliderWidget(contentX, startY, contentWidth, buttonHeight,
                Text.literal("Scale: " + String.format("%.2f", Config.INSTANCE.textScale)),
                (Config.INSTANCE.textScale - 0.1) / 0.9) {
            
            @Override
            protected void updateMessage() {
                double value = 0.1 + (this.value * 0.9);
                Config.INSTANCE.textScale = (float) value;

                setMessage(Text.literal("Scale: " + String.format("%.2f", value)));
            }

            @Override
            protected void applyValue() {}
        };
        addDrawableChild(scaleSlider);

        anchorButton = ButtonWidget.builder(
                Text.literal("Anchor: " + Config.INSTANCE.textAnchor.getDisplayName()),
                (button) -> {
                    anchorIndex = (anchorIndex + 1) % Config.TextAnchor.values().length;
                    Config.INSTANCE.textAnchor = Config.TextAnchor.values()[anchorIndex];

                    button.setMessage(Text.literal("Anchor: " + Config.INSTANCE.textAnchor.getDisplayName()));
                }).dimensions(contentX, startY + (buttonHeight + spacing), contentWidth, buttonHeight).build();
        addDrawableChild(anchorButton);

        colorButton = ButtonWidget.builder(
                Text.literal("Color: " + Config.INSTANCE.getColorName()),
                (button) -> {
                    colorIndex = (colorIndex + 1) % Config.COLOR_PALETTE.length;
                    Config.INSTANCE.textColor = Config.COLOR_PALETTE[colorIndex];

                    button.setMessage(Text.literal("Color: " + Config.INSTANCE.getColorName()));
                }).dimensions(contentX, startY + (buttonHeight + spacing) * 2, contentWidth, buttonHeight).build();
        addDrawableChild(colorButton);

        shadowButton = ButtonWidget.builder(
                Text.literal("Shadow: " + (Config.INSTANCE.textShadow ? "ON" : "OFF")),
                (button) -> {
                    Config.INSTANCE.textShadow = !Config.INSTANCE.textShadow;

                    boolean shadowEnabled = Config.INSTANCE.textShadow;

                    shadowButton.setMessage(Text.literal("Shadow: ").append(shadowEnabled ?
                                    Text.literal("ON") : Text.literal("OFF"))
                            .formatted(shadowEnabled ? Formatting.GREEN : Formatting.RED)
                    );
                }).dimensions(contentX, startY + (buttonHeight + spacing) * 3, contentWidth, buttonHeight).build();
        addDrawableChild(shadowButton);

        int bottomY = height - 25;
        int buttonWidth = Math.min(100, contentWidth / 2 - 5);
        int doneX = contentX + contentWidth - buttonWidth;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Reset"),
                (button) -> {
                    Config.INSTANCE.textScale = 0.5f;
                    Config.INSTANCE.textAnchor = Config.TextAnchor.TOP_LEFT;
                    Config.INSTANCE.textColor = 0xFFFFFF;
                    Config.INSTANCE.textShadow = false;

                    anchorIndex = 0;
                    colorIndex = 0;

                    remove(scaleSlider);

                    scaleSlider = new SliderWidget(contentX, startY, contentWidth, buttonHeight,
                            Text.literal("Scale: " + String.format("%.2f", Config.INSTANCE.textScale)),
                            (Config.INSTANCE.textScale - 0.1) / 0.9) {
                        @Override
                        protected void updateMessage() {
                            double value = 0.1 + (this.value * 0.9);
                            Config.INSTANCE.textScale = (float) value;
                            setMessage(Text.literal("Scale: " + String.format("%.2f", value)));
                        }

                        @Override
                        protected void applyValue() {}
                    };

                    addDrawableChild(scaleSlider);

                    anchorButton.setMessage(Text.literal("Anchor: " + Config.INSTANCE.textAnchor.getDisplayName()));
                    colorButton.setMessage(Text.literal("Color: " + Config.INSTANCE.getColorName()));

                    boolean shadowEnabled = Config.INSTANCE.textShadow;

                    shadowButton.setMessage(Text.literal("Shadow: ").append(shadowEnabled ?
                                    Text.literal("ON") : Text.literal("OFF"))
                            .formatted(shadowEnabled ? Formatting.GREEN : Formatting.RED)
                    );
                }).dimensions(contentX, bottomY, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                (button) -> {
                    Config.INSTANCE.save();

                    if(client == null) {
                        return;
                    }

                    client.setScreen(parent);
                }).dimensions(doneX, bottomY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Change Various Options For Inventory Slots"),
                width / 2, 40, 0xAAAAAA);

        int slotSize = 18;
        int slotContentSize = 16;
        int slotsPerRow = 3;
        int slotSpacing = 2;
        int totalWidth = (slotSize * slotsPerRow) + (slotSpacing * (slotsPerRow - 1));
        int startX = width / 2 - totalWidth / 2;

        int previewStartY = height / 2 + 50;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Preview"), width / 2, previewStartY - 12, 0xFFFFFF);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < slotsPerRow; col++) {
                int slotX = startX + col * (slotSize + slotSpacing);
                int slotY = previewStartY + row * (slotSize + slotSpacing);

                context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF8B8B8B);
                context.fill(slotX + 1, slotY + 1, slotX + slotSize - 1, slotY + slotSize - 1, 0xFF373737);

                int slotIndex = row * slotsPerRow + col;

                String previewText = String.valueOf(slotIndex);

                float scaledTextWidth = textRenderer.getWidth(previewText) * Config.INSTANCE.textScale;
                float scaledTextHeight = textRenderer.fontHeight * Config.INSTANCE.textScale;
                float textDrawX = Config.INSTANCE.textAnchor.getOffsetX(scaledTextWidth, slotContentSize);
                float textDrawY = Config.INSTANCE.textAnchor.getOffsetY(scaledTextHeight, slotContentSize);

                Matrix3x2fStack matrices = context.getMatrices();

                matrices.pushMatrix();
                matrices.translate(slotX + 1 + textDrawX, slotY + 1 + textDrawY);
                matrices.scale(Config.INSTANCE.textScale, Config.INSTANCE.textScale);

                if (Config.INSTANCE.textShadow) {
                    context.drawTextWithShadow(textRenderer, previewText, 0, 0, Config.INSTANCE.textColor);

                    matrices.popMatrix();
                    continue;
                }

                context.drawText(textRenderer, previewText, 0, 0, Config.INSTANCE.textColor, false);

                matrices.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        Config.INSTANCE.save();

        if(client == null) {
            return;
        }

        client.setScreen(parent);
    }

}
