package net.bitbylogic.inventoryslots.client.mixin;

import net.bitbylogic.inventoryslots.client.SlotLabelRenderer;
import net.bitbylogic.inventoryslots.client.SlotVisibility;
import net.bitbylogic.inventoryslots.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenWidgetMixin {

    @Inject(method = "renderWithTooltip", at = @At("TAIL"))
    private void onRenderWithTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.INSTANCE.enabled || !Config.INSTANCE.widgetNumbers || Config.INSTANCE.hotbarOnly) {
            return;
        }

        Screen screen = (Screen) (Object) this;

        if (screen instanceof HandledScreen<?> container && !SlotVisibility.screenAllowed(container)) {
            return;
        }

        context.createNewRootLayer();
        renderChildren(context, MinecraftClient.getInstance().textRenderer, screen.children());
    }

    private void renderChildren(DrawContext context, TextRenderer textRenderer, List<? extends Element> children) {
        for (int i = 0; i < children.size(); i++) {
            Element child = children.get(i);

            if (child instanceof ClickableWidget widget) {
                if (!widget.visible) {
                    continue;
                }

                SlotLabelRenderer.draw(context, textRenderer, String.valueOf(i), widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }

            if (child instanceof ParentElement parent) {
                renderChildren(context, textRenderer, parent.children());
            }
        }
    }

}
