package io.github.zaafonin.vs_oddities.mixin.biomes;

import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> adjustForWorldPosition(ServerLevel instance, BlockPos blockPos) {
        ServerLevel level = ServerLevel.class.cast(this);
        if (VSOdditiesConfig.Common.FIX_SHIP_WEATHER.get()) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
            if (ship != null) {
                Vector3d vPosWorld = ship.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOMLD(blockPos));
                BlockPos blockPosWorld = BlockPos.containing(vPosWorld.x, vPosWorld.y, vPosWorld.z);
                return level.getBiome(blockPosWorld);
            }
        }
        return level.getBiome(blockPos);
    }
}