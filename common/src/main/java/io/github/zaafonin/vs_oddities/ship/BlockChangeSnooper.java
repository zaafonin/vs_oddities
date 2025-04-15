package io.github.zaafonin.vs_oddities.ship;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An attachment implementing this interface will catch block state changes happening on the ship.
 */
public interface BlockChangeSnooper {
    /**
     * Called <i>before</i> the actual change happens.
     * TODO: maybe change this behavior?
     */
    public void onSetBlockState(ServerLevel serverLevel, BlockPos blockPos, BlockState oldBlockState, BlockState blockState);
}
