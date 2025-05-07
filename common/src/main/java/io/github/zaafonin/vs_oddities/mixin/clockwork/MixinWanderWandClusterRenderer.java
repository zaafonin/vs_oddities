package io.github.zaafonin.vs_oddities.mixin.clockwork;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronSelectionScreen;
import org.valkyrienskies.clockwork.content.curiosities.tools.wanderwand.WanderWandClusterRenderer;

@Mixin(value = WanderWandClusterRenderer.class, remap = false)
public abstract class MixinWanderWandClusterRenderer {
    @Inject(
            method = "renderDesignator",
            at = @At(
                    value = "NEW", target = "org/joml/Vector3i", ordinal = 0
            )
    )
    private void angelHover(
            ClientLevel level, Minecraft minecraft, PoseStack poseStack,
            CallbackInfo ci,
            @Local(ordinal = 0) LocalRef<BlockPos> hovered, @Local(ordinal = 1) LocalRef<BlockPos> hoveredFace,
            @Local AbstractClientPlayer player
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null) {
            HitResult pick = mc.hitResult;
            if (pick.getType() != HitResult.Type.BLOCK) {
                float pickDistance = 3;
                // Code from AngelBlockRenewed by LaidBackSloth.
                double x = player.getX() + player.getLookAngle().x * pickDistance;
                double y = player.getEyeY() + player.getLookAngle().y * pickDistance;
                double z = player.getZ() + player.getLookAngle().z * pickDistance;
                BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));

                hovered.set(pos);
                hoveredFace.set(pos);
            }
        }
    }
}
