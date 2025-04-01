package io.github.zaafonin.vs_oddities.mixin.biomes;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> adjustForWorldPosition(ServerLevel instance, BlockPos blockPos) {
        ServerLevel level = (ServerLevel)(Object)this;
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
        if (ship != null) {
            Vector3d vPosWorld = ship.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOMLD(blockPos));
            BlockPos blockPosWorld = BlockPos.containing(vPosWorld.x, vPosWorld.y, vPosWorld.z);
            return level.getBiome(blockPosWorld);
        }
        return level.getBiome(blockPos);
    }
}