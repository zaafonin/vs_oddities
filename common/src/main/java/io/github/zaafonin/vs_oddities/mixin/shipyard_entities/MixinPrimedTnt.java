package io.github.zaafonin.vs_oddities.mixin.shipyard_entities;

import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.handling.WorldEntityHandler;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(PrimedTnt.class)
public abstract class MixinPrimedTnt {
    // JANK: This mixin copies MixinEntity verbatim. Maybe this can be done with a multi-target mixin?
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    void leaveShipyard(CallbackInfo ci) {
        if (!VSOdditiesConfig.Common.SHIP_ENTITIES_LEAVE.get()) return;

        Entity e = Entity.class.cast(this);
        Ship ship = VSGameUtilsKt.getShipManaging(e);
        if (ship != null) {
            AABBic shipAABBi = ship.getShipAABB();
            AABBd shipAABB = new AABBd(
                    shipAABBi.minX(), shipAABBi.minY(), shipAABBi.minZ(),
                    shipAABBi.maxX(), shipAABBi.maxY(), shipAABBi.maxZ()
            );
            if (!shipAABB.containsPoint(VectorConversionsMCKt.toJOML(e.position()))) {
                WorldEntityHandler.INSTANCE.moveEntityFromShipyardToWorld(Entity.class.cast(this), ship);
            }
        }
    }
}