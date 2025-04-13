package io.github.zaafonin.vs_oddities.mixin.echochest;

import com.google.common.collect.MutableClassToInstanceMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.valkyrienskies.core.impl.game.ships.ShipData;

@Mixin(BlockPositionSource.class)
public interface AccessBlockPositionSource {
    @Accessor
    BlockPos getPos();
}