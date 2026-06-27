package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Final
    @Shadow protected T menu;

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void onExtractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (!Config.INSTANCE.enabled || Config.INSTANCE.hotbarOnly) {
            return;
        }

        Font font = getFont();
        float scale = Config.INSTANCE.textScale;

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);

            if (!slot.isActive()) {
                continue;
            }

            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;

            String text = String.valueOf(i);

            float textWidth = font.width(text) * scale;
            float textHeight = font.lineHeight * scale;

            float offsetX = Config.INSTANCE.textAnchor.getOffsetX(textWidth, 16);
            float offsetY = Config.INSTANCE.textAnchor.getOffsetY(textHeight, 16);

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
