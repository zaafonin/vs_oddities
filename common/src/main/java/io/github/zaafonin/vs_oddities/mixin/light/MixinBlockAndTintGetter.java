package io.github.zaafonin.vs_oddities.mixin.light;

import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.ship.OddAttachment;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.mixin.accessors.client.render.chunk.RenderChunkAccessor;

@Mixin(BlockAndTintGetter.class)
public interface MixinBlockAndTintGetter {
    @Shadow
    LevelLightEngine getLightEngine();

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    default void applyWorldBrightness(LightLayer lightType, BlockPos blockPos, CallbackInfoReturnable<Integer> ci) {
        if (!VSOdditiesConfig.Common.FIX_SHIP_LIGHTING.get()) return;

        // Cursed.
        Level level = null;
        if (this instanceof Level) {
            level = (Level) this;
        } else if (this instanceof RenderChunkRegion) {
            level = ((IMixinRenderChunkRegion) this).getLevel();
        }

        if (level != null) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
            if (ship != null) {
                Vector3d vPosWorld = ship.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOMLD(blockPos));
                // BlockPos.containing does not work here.
                BlockPos blockPosWorld = OddUtils.toNearestBlock(vPosWorld);
                switch (lightType) {
                    case BLOCK:
                        // Block lighting: combine on-ship light with in-world.
                        ci.setReturnValue(Math.min(
                                this.getLightEngine().getLayerListener(lightType).getLightValue(blockPos)
                                + this.getLightEngine().getLayerListener(lightType).getLightValue(blockPosWorld),
                                15)
                        );
                        break;
                    case SKY:
                        // Sky lighting: lower lighting indicates sky occlusion. Choose that one.
                        ci.setReturnValue(Math.min(
                                this.getLightEngine().getLayerListener(lightType).getLightValue(blockPos),
                                this.getLightEngine().getLayerListener(lightType).getLightValue(blockPosWorld)
                        ));
                        break;
                }
                ci.cancel();
            }
        }
    }
}