package io.github.zaafonin.vs_oddities.mixin.shipyard_entities;

import net.minecraft.world.phys.AABB;
import org.joml.primitives.AABBic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.handling.WorldEntityHandler;

@Mixin(value = Entity.class, priority = 1200)
public abstract class MixinEntity {
    // Move shipyard entities to world once they quit the ship
    @Inject(
            // JANK: Bad injection target. This is called for entities, though.
            method = "checkBelowWorld",
            at = @At("HEAD")
    )
    void leaveShipyard(CallbackInfo ci) {
        Entity e = Entity.class.cast(this);
        Ship ship = VSGameUtilsKt.getShipManaging(e);
        if (ship != null) {
            AABBic shipAABBi = ship.getShipAABB();
            AABB eBB = e.getBoundingBox();
            AABB shipAABB = new AABB(
                    shipAABBi.minX(), shipAABBi.minY(), shipAABBi.minZ(),
                    shipAABBi.maxX(), shipAABBi.maxY(), shipAABBi.maxZ()
            ).inflate(-0.25, 1.5, -0.25); // don't ask

            // JANK: If the ship does not protrude vertically, entities like minecarts will end up on top of the ship,
            // and thus outside of its bounding box. If we expand the bounding box too much, our minecarts will dismount
            // the ship rather late. We can't use e.isOnRails() as it's reset like once a second. Combined with the
            // minecarts joining the ships once they are close enough, an early dismount will result in an infinite loop
            // which is really the worst case, especially if some other entity is riding the minecart.

            // Right now the compromise is to extend the ship AABB just a bit and do other checks.
            if (!shipAABB.intersects(eBB) && e.flyDist > 0.05) {
                WorldEntityHandler.INSTANCE.moveEntityFromShipyardToWorld(Entity.class.cast(this), ship);
            }
        }
    }
}