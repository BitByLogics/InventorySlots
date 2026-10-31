package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.client.SlotLabelRenderer;
import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenWidgetMixin {

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void onExtractRenderStateWithTooltipAndSubtitles(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.INSTANCE.enabled || !Config.INSTANCE.widgetNumbers) {
            return;
        }

        Screen screen = (Screen) (Object) this;

        graphics.nextStratum();
        extractChildren(graphics, screen.getFont(), screen.children());
    }

    private void extractChildren(GuiGraphicsExtractor graphics, Font font, List<? extends GuiEventListener> children) {
        for (int i = 0; i < children.size(); i++) {
            GuiEventListener child = children.get(i);

            if (child instanceof AbstractWidget widget) {
                if (!widget.visible) {
                    continue;
                }

                SlotLabelRenderer.draw(graphics, font, String.valueOf(i), widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }

            if (child instanceof ContainerEventHandler container) {
                extractChildren(graphics, font, container.children());
            }
        }
    }

}
