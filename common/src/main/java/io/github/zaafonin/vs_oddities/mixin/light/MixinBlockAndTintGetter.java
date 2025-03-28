package io.github.zaafonin.vs_oddities.mixin.light;

import io.github.zaafonin.vs_oddities.ship.OddAttachment;
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
public abstract interface MixinBlockAndTintGetter {
    @Shadow
    abstract LevelLightEngine getLightEngine();

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    default void getBrightness(LightLayer lightType, BlockPos blockPos, CallbackInfoReturnable<Integer> ci) {
        Level level = getLevel();
        if (level != null) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
            if (ship != null) {
                Vector3d vPosWorld = ship.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOMLD(blockPos));
                BlockPos blockPosWorld = new BlockPos((int) (vPosWorld.x + 0.5), (int) (vPosWorld.y + 0.5), (int) (vPosWorld.z + 0.5));
                switch (lightType) {
                    case BLOCK:
                        ci.setReturnValue(
                                (this.getLightEngine().getLayerListener(lightType).getLightValue(blockPos)
                                        + this.getLightEngine().getLayerListener(lightType).getLightValue(blockPosWorld) % 16)
                        );
                        break;
                    case SKY:
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

    /*@Inject(method = "getRawBrightness", at = @At("HEAD"), cancellable = true)
    default void getRawBrightness(BlockPos blockPos, int amount, CallbackInfoReturnable<Integer> ci) {
        Level level = getLevel();
        if (level != null) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
            if (ship != null) {
                ci.setReturnValue(0);
                ci.cancel();
            }
        }
    }*/

    // Cursed.
    private Level getLevel() {
        Level level = null;
        if (this instanceof Level) {
            level = (Level)this;
        } else if (this instanceof RenderChunkRegion) {
            level = ((IMixinRenderChunkRegion)this).getLevel();
        }
        return level;
    }
}