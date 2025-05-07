package io.github.zaafonin.vs_oddities.mixin.clockwork;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronSelectionScreen;

@Mixin(value = GravitronSelectionScreen.class, remap = false)
public abstract class MixinGravitronSelectionScreen {
    @Inject(method = "draw", at = @At("TAIL"))
    private void fixAlpha(GuiGraphics graphics, float partialTicks, CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
