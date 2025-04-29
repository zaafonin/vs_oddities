package io.github.zaafonin.vs_oddities.mixin.f3;

import com.google.common.collect.MutableClassToInstanceMap;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.zaafonin.vs_oddities.mixin.mass.AccessMassDatapackResolver;
import io.github.zaafonin.vs_oddities.mixin.vs2.AccessShipData;
import io.github.zaafonin.vs_oddities.ship.DebugPresentable;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.MassDatapackResolver;
import org.valkyrienskies.mod.common.util.EntityDraggingInformation;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

import java.util.List;
import java.util.Locale;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private HitResult block;
    @Shadow
    protected abstract Level getLevel();

    @Inject(method = "getGameInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private void addShipCountInformation(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        QueryableShipData<Ship> ships = VSGameUtilsKt.getAllShips(getLevel());
        list.add("Ships: " + ships.size());
    }

    @Inject(method = "getGameInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 6, shift = At.Shift.AFTER))
    private void addPlayerDraggingInformation(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        EntityDraggingInformation info = ((IEntityDraggingInformationProvider)minecraft.player).getDraggingInformation();
        if (info != null) {
            if (info.isEntityBeingDraggedByAShip()) {
                Long shipId = info.getLastShipStoodOn();
                if (shipId != null) {
                    Ship ship = VSGameUtilsKt.getAllShips(getLevel()).getById(shipId);
                    if (ship != null) {
                        list.add("Dragged by: " + VSGameUtilsKt.getAllShips(getLevel()).getById(info.getLastShipStoodOn()).getSlug());
                    }
                }
            }
        }
    }

    @Inject(
            method = "getSystemInformation",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V",
                    ordinal = 0 // Also called for picked fluids, not interesting for us.
            )
    )
    private void addVSBlockProperties(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        if (this.block.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) this.block).getBlockPos();
            BlockState blockState = this.minecraft.level.getBlockState(blockPos);
            Object o = AccessMassDatapackResolver.class.cast(MassDatapackResolver.INSTANCE).getMap().get(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()));
            if (o != null) {
                // As good as we can get. VSBlockStateInfo is private class so fields are inaccessible.
                list.add(String.format(
                        Locale.ROOT,
                        "(%s",
                        o.toString().split("[(]")[1]
                ));
            }
        }
    }

    @Inject(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private void addShipInformation(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        Level l = getLevel();
        BlockPos blockPos = ((BlockHitResult) this.block).getBlockPos();
        Ship ship = VSGameUtilsKt.getShipManagingPos(l, blockPos);
        LoadedServerShip lsship = l instanceof ServerLevel ? VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) l, blockPos) : null;
        if (ship != null) {
            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Ship: " + ship.getSlug());
            if (lsship != null) {
                // Seems like ship's physical behavior is none of client's business.
                list.add("Static: " + lsship.isStatic());
                list.add("Mass: " + lsship.getInertiaData().getMass() + " kg");
                list.add("");
                // Sus: accessor to vscore.
                MutableClassToInstanceMap<?> attachments = ((AccessShipData)((ShipObjectServer)lsship).asShipDataCommon()).getPersistentAttachedData();
                attachments.forEach((k, v) -> {
                    list.add(ChatFormatting.UNDERLINE + k.toString());
                    if (v instanceof DebugPresentable) {
                        ((DebugPresentable)v).addDebugLines(list);
                    }
                });
            }
            Vector3dc scale = ship.getTransform().getShipToWorldScaling();
            if (!scale.equals(new Vector3d(1, 1, 1), 0.0005)) {
                // Dealing with a scaled ship.
                list.add(String.format(
                        Locale.ROOT,
                        "Ship Scale: %.3f / %.3f / %.3f",
                        scale.x(),
                        scale.y(),
                        scale.z()
                ));
            }
            Vector3dc linVel = ship.getVelocity();
            list.add(String.format(
                    Locale.ROOT,
                    "Linear Velocity: %.3f / %.3f / %.3f, total: %.3f m/s",
                    linVel.x(),
                    linVel.y(),
                    linVel.z(),
                    linVel.length()
            ));
            Vector3dc angVel = ship.getOmega();
            list.add(String.format(
                    Locale.ROOT,
                    "Angular Velocity: %.3f / %.3f / %.3f",
                    angVel.x(),
                    angVel.y(),
                    angVel.z()
            ));
            Vec3 pos = OddUtils.toWorldCoordinates(ship, blockPos.getCenter());
            list.add(String.format(
                    Locale.ROOT,
                    "Targeted World Position: %.3f / %.3f / %.3f",
                    pos.x(),
                    pos.y(),
                    pos.z()
            ));
        }
    }
}