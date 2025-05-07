package io.github.zaafonin.vs_oddities.mixin.echochest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockPositionSource.class)
public interface AccessBlockPositionSource {
    @Accessor
    BlockPos getPos();
}