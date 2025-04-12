package io.github.zaafonin.vs_oddities.ship;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockChangeSnooper {
    public void onSetBlockState(ServerLevel serverLevel, BlockPos blockPos, BlockState oldBlockState, BlockState blockState);
}
