package net.bitbylogic.inventoryslots.client.config;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

public class ConfigScreen extends Screen {

    private static final Component ON_TEXT = Component.literal("ON").withStyle(ChatFormatting.GREEN);
    private static final Component OFF_TEXT = Component.literal("OFF").withStyle(ChatFormatting.RED);

    private final Screen parent;

    private Button anchorButton;
    private Button colorButton;
    private Button shadowButton;
    private Button hotbarButton;
    private Button hotbarOnlyButton;
    private AbstractSliderButton scaleSlider;

    private int anchorIndex;
    private int colorIndex = 0;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Inventory Slots Config"));

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
        int optionWidth = 180;
        int optionHeight = 20;

        int leftX = ((width - 5) >>> 1) - optionWidth;
        int rightX = (width + 5) >>> 1;

        int y = 60;

        anchorButton = Button.builder(
                Component.literal("Anchor: " + Config.INSTANCE.textAnchor.getDisplayName()),
                (button) -> {
                    anchorIndex = (anchorIndex + 1) % Config.TextAnchor.values().length;
                    Config.INSTANCE.textAnchor = Config.TextAnchor.values()[anchorIndex];

                    button.setMessage(Component.literal("Anchor: " + Config.INSTANCE.textAnchor.getDisplayName()));
                }).bounds(leftX, y, optionWidth, optionHeight).build();
        addRenderableWidget(anchorButton);

        colorButton = Button.builder(
                Component.literal("Color: " + Config.INSTANCE.getColorName()),
                (button) -> {
                    colorIndex = (colorIndex + 1) % Config.COLOR_PALETTE.length;
                    Config.INSTANCE.textColor = Config.COLOR_PALETTE[colorIndex];

                    button.setMessage(Component.literal("Color: " + Config.INSTANCE.getColorName()));
                }).bounds(rightX, y, optionWidth, optionHeight).build();
        addRenderableWidget(colorButton);
        y += 25;

        shadowButton = Button.builder(
                Component.literal("Shadow: ").append(Config.INSTANCE.textShadow ? ON_TEXT : OFF_TEXT),
                (button) -> {
                    Config.INSTANCE.textShadow = !Config.INSTANCE.textShadow;

                    boolean shadowEnabled = Config.INSTANCE.textShadow;

                    shadowButton.setMessage(Component.literal("Shadow: ")
                            .append(shadowEnabled ? ON_TEXT : OFF_TEXT));
                }).bounds(leftX, y, optionWidth, optionHeight).build();
        addRenderableWidget(shadowButton);

        scaleSlider = new AbstractSliderButton(rightX, y, optionWidth, optionHeight,
                Component.literal("Scale: " + String.format("%.2f", Config.INSTANCE.textScale)),
                (Config.INSTANCE.textScale - 0.1) / 0.9) {

            @Override
            protected void updateMessage() {
                double value = 0.1 + (this.value * 0.9);
                Config.INSTANCE.textScale = (float) value;

                setMessage(Component.literal("Scale: " + String.format("%.2f", value)));
            }

            @Override
            protected void applyValue() {}
        };
        addRenderableWidget(scaleSlider);
        y += 25;

        hotbarButton = Button.builder(
                Component.literal("Hotbar Numbers: ").append(Config.INSTANCE.hotbarNumbers ? ON_TEXT : OFF_TEXT),
                (button) -> {
                    Config.INSTANCE.hotbarNumbers = !Config.INSTANCE.hotbarNumbers;

                    boolean hotbarNumbers = Config.INSTANCE.hotbarNumbers;

                    hotbarButton.setMessage(Component.literal("Hotbar Numbers: ")
                            .append(hotbarNumbers ? ON_TEXT : OFF_TEXT));
                }).bounds(leftX, y, optionWidth, optionHeight).build();
        addRenderableWidget(hotbarButton);

        hotbarOnlyButton = Button.builder(
                Component.literal("Hotbar Only: ").append(Config.INSTANCE.hotbarOnly ? ON_TEXT : OFF_TEXT),
                (button) -> {
                    Config.INSTANCE.hotbarOnly = !Config.INSTANCE.hotbarOnly;

                    boolean hotbarOnly = Config.INSTANCE.hotbarOnly;

                    if(!Config.INSTANCE.hotbarNumbers) {
                        Config.INSTANCE.hotbarNumbers = true;
                        hotbarButton.setMessage(Component.literal("Hotbar Numbers: ").append(ON_TEXT));
                    }

                    hotbarOnlyButton.setMessage(Component.literal("Hotbar Only: ")
                            .append(hotbarOnly ? ON_TEXT : OFF_TEXT));
                }).bounds(rightX, y, optionWidth, optionHeight).build();
        addRenderableWidget(hotbarOnlyButton);

        int bottomY = height - 25;
        int finalY = y;

        addRenderableWidget(Button.builder(
                Component.literal("Reset"),
                (button) -> {
                    Config.INSTANCE.textScale = 0.5f;
                    Config.INSTANCE.textAnchor = Config.TextAnchor.TOP_LEFT;
                    Config.INSTANCE.textColor = 0xFFFFFF;
                    Config.INSTANCE.textShadow = false;
                    Config.INSTANCE.hotbarNumbers = false;
                    Config.INSTANCE.hotbarOnly = false;

                    anchorIndex = 0;
                    colorIndex = 0;

                    removeWidget(scaleSlider);

                    scaleSlider = new AbstractSliderButton(leftX, finalY, optionWidth, optionHeight,
                            Component.literal("Scale: " + String.format("%.2f", Config.INSTANCE.textScale)),
                            (Config.INSTANCE.textScale - 0.1) / 0.9) {
                        @Override
                        protected void updateMessage() {
                            double value = 0.1 + (this.value * 0.9);
                            Config.INSTANCE.textScale = (float) value;
                            setMessage(Component.literal("Scale: " + String.format("%.2f", value)));
                        }

                        @Override
                        protected void applyValue() {}
                    };

                    addRenderableWidget(scaleSlider);

                    anchorButton.setMessage(Component.literal("Anchor: " + Config.INSTANCE.textAnchor.getDisplayName()));
                    colorButton.setMessage(Component.literal("Color: " + Config.INSTANCE.getColorName()));

                    boolean shadowEnabled = Config.INSTANCE.textShadow;
                    boolean hotbarEnabled = Config.INSTANCE.hotbarNumbers;
                    boolean hotbarOnly = Config.INSTANCE.hotbarOnly;

                    shadowButton.setMessage(Component.literal("Shadow: ").append(shadowEnabled ? ON_TEXT : OFF_TEXT));
                    hotbarButton.setMessage(Component.literal("Hotbar Numbers: ").append(hotbarEnabled ? ON_TEXT : OFF_TEXT));
                    hotbarOnlyButton.setMessage(Component.literal("Hotbar Only: ").append(hotbarOnly ? ON_TEXT : OFF_TEXT));
                }).bounds(leftX, bottomY, optionWidth, optionHeight).build());

        addRenderableWidget(Button.builder(
                Component.literal("Done"),
                (button) -> {
                    Config.INSTANCE.save();

                    if(minecraft == null) {
                        return;
                    }

                    minecraft.setScreen(parent);
                }).bounds(rightX, bottomY, optionWidth, optionHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(font, title, width / 2, 20, ARGB.opaque(0xFFFFFF));
        graphics.centeredText(font,
                Component.literal("Change Various Options For Inventory Slots"),
                width / 2, 40, ARGB.opaque(0xAAAAAA));

        int slotSize = 18;
        int slotContentSize = 16;
        int slotsPerRow = 3;
        int slotSpacing = 2;
        int totalWidth = (slotSize * slotsPerRow) + (slotSpacing * (slotsPerRow - 1));
        int startX = width / 2 - totalWidth / 2;

        int previewStartY = height / 2 + 50;
        graphics.centeredText(font, Component.literal("Preview"), width / 2, previewStartY - 12, ARGB.opaque(0xFFFFFF));

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < slotsPerRow; col++) {
                int slotX = startX + col * (slotSize + slotSpacing);
                int slotY = previewStartY + row * (slotSize + slotSpacing);

                graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF8B8B8B);
                graphics.fill(slotX + 1, slotY + 1, slotX + slotSize - 1, slotY + slotSize - 1, 0xFF373737);

                int slotIndex = row * slotsPerRow + col;

                String previewText = String.valueOf(slotIndex);

                float scaledTextWidth = font.width(previewText) * Config.INSTANCE.textScale;
                float scaledTextHeight = font.lineHeight * Config.INSTANCE.textScale;
                float textDrawX = Config.INSTANCE.textAnchor.getOffsetX(scaledTextWidth, slotContentSize);
                float textDrawY = Config.INSTANCE.textAnchor.getOffsetY(scaledTextHeight, slotContentSize);

                Matrix3x2fStack matrices = graphics.pose();

                matrices.pushMatrix();
                matrices.translate(slotX + 1 + textDrawX, slotY + 1 + textDrawY);
                matrices.scale(Config.INSTANCE.textScale, Config.INSTANCE.textScale);

                graphics.text(font, previewText, 0, 0, ARGB.opaque(Config.INSTANCE.textColor), Config.INSTANCE.textShadow);

                matrices.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        Config.INSTANCE.save();

        if(minecraft == null) {
            return;
        }

        minecraft.setScreen(parent);
    }

}