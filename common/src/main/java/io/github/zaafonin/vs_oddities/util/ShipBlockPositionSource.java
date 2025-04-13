package io.github.zaafonin.vs_oddities.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ShipBlockPositionSource extends BlockPositionSource {
    final BlockPos pos;

    @Override
    public Optional<Vec3> getPosition(Level level) {
        return Optional.of(OddUtils.getWorldCoordinates(level, Vec3.atCenterOf(pos)));
    }

    public ShipBlockPositionSource(BlockPos blockPos) {
        super(blockPos);
        pos = blockPos;
    }
}
