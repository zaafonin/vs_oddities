package io.github.zaafonin.vs_oddities.mixin.draught_mobs;

import io.github.zaafonin.vs_oddities.ship.ThrustInducer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(PathfinderMob.class)
public abstract class MixinPathfinderMob {
    @Unique
    double vs_oddities$prevDistance = 0;

    // Used by Entity.restrictTo();
    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/PathfinderMob;onLeashDistance(F)V"
            )
    )
    void dragShip(PathfinderMob mob, float f) {
        // This is only called when the lead is stretched.
        f = Math.max(0, f - 4);

        if (!mob.level().isClientSide) { // VS2 forces can only be applied to server ships.
            Entity leash = mob.getLeashHolder();

            LoadedServerShip ship = (LoadedServerShip)VSGameUtilsKt.getShipObjectManagingPos(leash.level(), leash.blockPosition());
            if (ship != null && !ship.isStatic()) {
                if (true || f > vs_oddities$prevDistance) {
                    Vec3 leashWorldPos = VSGameUtilsKt.toWorldCoordinates(ship, leash.position());
                    double d = (leashWorldPos.x - mob.getX()) / f;
                    double e = (leashWorldPos.y - mob.getY()) / f;
                    double g = (leashWorldPos.z - mob.getZ()) / f;
                    Vec3 stretch = new Vec3(Math.copySign(d * d * 0.4, d), Math.copySign(e * e * 0.4, e), Math.copySign(g * g * 0.4, g));
                    //this.setDeltaMovement(this.getDeltaMovement().add(Math.copySign(d * d * 0.4, d), Math.copySign(e * e * 0.4, e), Math.copySign(g * g * 0.4, g)));

                    double mobStrengthFactor = -200000;

                    Vector3d JOMLposInShip = VectorConversionsMCKt.toJOML(leash.position()).sub(ship.getTransform().getPositionInShip());
                    Vector3d JOMLforce = VectorConversionsMCKt.toJOML(stretch).mul(mobStrengthFactor);

                    // Cap the force so we don't accelerate the ship beyond mob's own speed.
                    double maxSpeed = mob.getSpeed(); // blocks per 1/20 s
                    double shipVel = ship.getVelocity().length(); // block per s
                    // 3 physics ticks per game tick -> force of 60 N applied for 1/60 s would accelerate a 1 kg ship by 1 m/s.
                    double maxForce = (maxSpeed * 60) * ship.getInertiaData().getMass() * 1.5;
                    if (JOMLforce.length() > maxForce) {
                        JOMLforce.normalize(maxForce);
                    }

                    ThrustInducer applier = ThrustInducer.getOrCreate(ship);
                    if (!JOMLforce.isFinite()) return; // Something went really wrong with our calculations.

                    applier.applyPulse(JOMLforce, JOMLposInShip, 1, true);
                }
            }

            vs_oddities$prevDistance = f;
        }
    }
}