package io.github.zaafonin.vs_oddities.mixin.vs2;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.util.VectorConversionsKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.handling.VSEntityManager;
import org.valkyrienskies.mod.common.entity.handling.WorldEntityHandler;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Iterator;

@Mixin(AbstractMinecart.class)
public abstract class MixinAbstractMinecart extends MixinEntity {
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
        AbstractMinecart e = AbstractMinecart.class.cast(this);
        System.out.println("Current position: " + e.position().toString());
        System.out.println("Handled by: " + VSEntityManager.INSTANCE.getHandler(e));
        BlockPos potentialPos = new BlockPos(k.get(), i.get() - 1, j.get());
        if (!e.level().getBlockState(potentialPos).is(BlockTags.RAILS) && !VSGameUtilsKt.isBlockInShipyard(e.level(), potentialPos)) {
            System.out.println("Not on rails...");
            AABB minecartAABB = new AABB(potentialPos);
            Iterable<Ship> ships = VSGameUtilsKt.getShipsIntersecting(e.level(), minecartAABB);
            do {
                Iterator<Ship> shipIt = ships.iterator();
                if (shipIt.hasNext()) {
                    Ship ship = shipIt.next();
                    System.out.println("Found a ship! " + ship.getSlug());
                    if (ship != null) {
                        Vector3dc shipPos = ship.getWorldToShip().transformPosition(VectorConversionsMCKt.toJOML(e.position()));
                        BlockPos shipBlockPos = BlockPos.containing(shipPos.x(), shipPos.y(), shipPos.z());
                        if (e.level().getBlockState(shipBlockPos).is(BlockTags.RAILS)) {
                            System.out.println("Fitting rail for the ship: " + shipBlockPos.toString());
                            // TODO: Somehow bypass the VS2 mixin for setting position (can't really teleport to shipyard)
                            //k.set(shipBlockPos.getX());
                            //i.set(shipBlockPos.getY());
                            //j.set(shipBlockPos.getZ());
                            e.teleportTo(shipPos.x(), shipPos.y(), shipPos.z());
                            //((AccessEntity) e).setPosition(VectorConversionsMCKt.toMinecraft(shipPos).add(0, 1, 0));
                        }
                    }
                }
            } while (false);
        } else {
            System.out.println("World rails are fine.");
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
        if (VSGameUtilsKt.getShipManagingPos(en.level(), en.position()) != VSGameUtilsKt.getShipManagingPos(en.level(), d, e, f)) {
            en.setPos(d, e, f);
            // TODO: Some jank might be related to me not setting any kind of lStep, will fix later today.
            // original.call(d, e, f, g, h, 1, bl);
        } else {
            original.call(d, e, f, g, h, i, bl);
        }
    }
}