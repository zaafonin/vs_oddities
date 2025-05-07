package io.github.zaafonin.vs_oddities.mixin.shipyard_entities;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Iterator;

@Mixin(AbstractMinecart.class)
public abstract class MixinAbstractMinecart extends MixinEntity {
    @Unique
    long lastShip = 0;
    @Unique
    long lastShipCooldown = 0;

    // Snap world minecarts to ship rails.
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    void alsoCheckShip(
            CallbackInfo ci,
            @Local(ordinal = 0) LocalIntRef k,
            @Local(ordinal = 1) LocalIntRef i,
            @Local(ordinal = 2) LocalIntRef j
    ) {
        if (!VSOdditiesConfig.Common.SHIP_MINECARTS_SNAP.get()) return;

        if (lastShipCooldown > 0) {
            lastShipCooldown--;
        } else {
            lastShip = 0;
            lastShipCooldown = 0;
        }
        AbstractMinecart e = AbstractMinecart.class.cast(this);
        if (e.isOnRails()) return; // Only search for new rails if the cart is derailed.

        BlockPos potentialPos = new BlockPos(k.get(), i.get() - 1, j.get());
        if (!e.level().getBlockState(potentialPos).is(BlockTags.RAILS) && !VSGameUtilsKt.isBlockInShipyard(e.level(), potentialPos)) {
            AABB minecartAABB = new AABB(potentialPos);
            Iterable<Ship> ships = VSGameUtilsKt.getShipsIntersecting(e.level(), minecartAABB);
            do {
                Iterator<Ship> shipIt = ships.iterator();
                if (shipIt.hasNext()) {
                    Ship ship = shipIt.next();
                    if (ship != null && ship.getId() != lastShip) {
                        Vector3dc shipPos = ship.getWorldToShip().transformPosition(VectorConversionsMCKt.toJOML(e.position()));
                        BlockPos shipBlockPos = BlockPos.containing(shipPos.x(), shipPos.y(), shipPos.z());
                        BlockPos shipBlockPos1 = BlockPos.containing(shipPos.x(), shipPos.y() - 1, shipPos.z());
                        if (
                                e.level().getBlockState(shipBlockPos).is(BlockTags.RAILS)
                                ||
                                e.level().getBlockState(shipBlockPos1).is(BlockTags.RAILS)
                        ) {
                            //OddUtils.moveEntityFromWorldToShipyard(e, ship, e.getX(), e.getY(), e.getZ());
                            e.teleportTo(shipPos.x(), shipPos.y(), shipPos.z());

                            lastShip = ship.getId();
                            lastShipCooldown = 5;
                        }
                    }

                }
            } while (false);
        }
    }

    /**
     * Copy of MixinClientPacketListener.java from VS2.
     * Minecarts override lerpTo(), this is why the VS2 mixin does not work for them.
     * The reason this mixin is necessary is pretty much the same.
     */
    @WrapMethod(
            method = "lerpTo"
    )
    void dumbLerpIfToShipyard(
            double d, double e, double f, float g, float h, int i, boolean bl, Operation<Void> original
    ) {
        Entity en = Entity.class.cast(this);
        if (VSOdditiesConfig.Common.SHIP_MINECARTS_SNAP.get() && (VSGameUtilsKt.getShipManagingPos(en.level(), en.position()) != VSGameUtilsKt.getShipManagingPos(en.level(), d, e, f))) {
            en.setPos(d, e, f);
            original.call(d, e, f, g, h, -1, bl);
        } else {
            original.call(d, e, f, g, h, i, bl);
        }
    }
}