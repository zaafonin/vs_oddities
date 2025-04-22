package io.github.zaafonin.vs_oddities.mixin.shipyard_entities;

import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(PathfinderMob.class)
public abstract class MixinPathfinderMob {
    // Used by Entity.restrictTo();
    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;blockPosition()Lnet/minecraft/core/BlockPos;"
            )
    )
    BlockPos worldAwareRestrictPos(Entity entity) {
        BlockPos entityBlockPos = entity.blockPosition();
        Ship shipThis = VSGameUtilsKt.getShipManaging(Entity.class.cast(this));
        Ship shipOther = VSGameUtilsKt.getShipManaging(entity);
        if (shipThis != shipOther) {
            if (shipOther != null) {
                return OddUtils.toNearestBlock(VSGameUtilsKt.toWorldCoordinates(shipOther, entity.position()));
            }
        }
        return entityBlockPos;
    }

    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/PathfinderMob;distanceTo(Lnet/minecraft/world/entity/Entity;)F"
            )
    )
    float worldAwareDistanceTo(PathfinderMob instance, Entity entity) {
        return (float)
                VSGameUtilsKt.toWorldCoordinates(instance.level(), instance.position())
                .distanceTo(
                VSGameUtilsKt.toWorldCoordinates(entity.level(), entity.position()));
    }

    // BAD CODE
    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = -1
            )
    )
    double worldAwareLeashX(Entity instance) {
        if (instance != Entity.class.cast(this)) {
            return VSGameUtilsKt.toWorldCoordinates(instance.level(), instance.position()).x;
        }
        return instance.getX();
    }

    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getY()D",
                    ordinal = -1
            )
    )
    double worldAwareLeashY(Entity instance) {
        if (instance != Entity.class.cast(this)) {
            return VSGameUtilsKt.toWorldCoordinates(instance.level(), instance.position()).y;
        }
        return instance.getY();
    }

    @Redirect(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getZ()D",
                    ordinal = -1
            )
    )
    double worldAwareLeashZ(Entity instance) {
        if (instance != Entity.class.cast(this)) {
            return VSGameUtilsKt.toWorldCoordinates(instance.level(), instance.position()).z;
        }
        return instance.getZ();
    }
}