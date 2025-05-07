package io.github.zaafonin.vs_oddities.mixin.snoop;

import io.github.zaafonin.vs_oddities.ship.LiftDragInducer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {
    @Shadow
    @Final
    Level level;

    @Shadow
    public abstract BlockState getBlockState(net.minecraft.core.BlockPos blockPos);

    @Inject(method = "setBlockState", at = @At("HEAD")) // need to fetch the old block state
    private void trackOddProperties(BlockPos blockPos, BlockState blockState, boolean bl, CallbackInfoReturnable<BlockState> ci) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Using ServerShip instead of LoadedServerShip for reasons similar to the biome mixins.
        // The ship is seemingly not yet loaded when it is just created.
        ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);
        if (ship == null) return;

        BlockState oldBlockState = getBlockState(blockPos);

        LiftDragInducer.getOrCreate(ship).onSetBlockState(serverLevel, blockPos, oldBlockState, blockState);
        /*MutableClassToInstanceMap<?> attachments = ((AccessShipData)ship).getPersistentAttachedData();
        attachments.forEach((k, v) -> {
            if (v instanceof BlockChangeSnooper) {
                ((BlockChangeSnooper) v).onSetBlockState(serverLevel, blockPos, oldBlockState, blockState);
            }
        });*/
    }
}
