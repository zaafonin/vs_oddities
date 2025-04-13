package io.github.zaafonin.vs_oddities.mixin.vs2;

import com.google.common.collect.MutableClassToInstanceMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.valkyrienskies.core.impl.game.ships.ShipData;

@Mixin(value = ShipData.class, remap = false)
public interface AccessShipData {
    @Accessor
    MutableClassToInstanceMap<?> getPersistentAttachedData();
}