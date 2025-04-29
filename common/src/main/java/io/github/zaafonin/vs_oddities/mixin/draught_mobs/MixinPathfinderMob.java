package io.github.zaafonin.vs_oddities.mixin.draught_mobs;

import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.ship.ThrustInducer;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
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
    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/PathfinderMob;onLeashDistance(F)V"
            )
    )
    void dragShip(PathfinderMob mob, float f) {
        if (!VSOdditiesConfig.Common.ODDITIES_DRAUGHT_MOBS.get()) return;

        // f = Math.max(0, f - 3);
        if (!mob.level().isClientSide) { // VS2 forces can only be applied to server ships.
            Entity leash = mob.getLeashHolder();

            LoadedServerShip ship = (LoadedServerShip)VSGameUtilsKt.getShipObjectManagingPos(leash.level(), leash.blockPosition());
            if (ship != null && !ship.isStatic()) {
                Vec3 leashWorldPos = VSGameUtilsKt.toWorldCoordinates(ship, leash.position());
                double d = (leashWorldPos.x - mob.getX());
                double e = (leashWorldPos.y - mob.getY());
                double g = (leashWorldPos.z - mob.getZ());
                Vec3 stretch = new Vec3(Math.copySign(d * d * 0.4, d), Math.copySign(e * e * 0.4, e), Math.copySign(g * g * 0.4, g));
                Vector3d JOMLforce = VectorConversionsMCKt.toJOML(stretch).mul(ship.getInertiaData().getMass() / -1);
                ship.getVelocity();

                // Cap the force. Horses are strong but not "drag around a cubic meter of netherite" strong.
                // TODO: Mob strength values should be defined as data, like vs_mass.json.
                double mobStrength = 200000;
                if (JOMLforce.length() > mobStrength) {
                    JOMLforce.normalize(mobStrength);
                }

                Vector3d JOMLposInShip = VectorConversionsMCKt.toJOML(leash.position()).sub(ship.getTransform().getPositionInShip());

                ThrustInducer applier = ThrustInducer.getOrCreate(ship);
                if (!JOMLforce.isFinite()) return; // Something went really wrong with our calculations.

                applier.applyPulse(JOMLforce, null, JOMLposInShip, 3, true);
            }
        }
    }
}