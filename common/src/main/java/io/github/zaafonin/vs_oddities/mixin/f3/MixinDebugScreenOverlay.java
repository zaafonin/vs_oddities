package io.github.zaafonin.vs_oddities.mixin.f3;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedServerShipKt;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
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
    abstract Level getLevel();

    @Inject(method = "getGameInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 6, shift = At.Shift.AFTER))
    private void addPlayerShipInformation(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        EntityDraggingInformation info = ((IEntityDraggingInformationProvider)minecraft.player).getDraggingInformation();
        if (info != null) {
            if (info.isEntityBeingDraggedByAShip()) {
                list.add("Dragged by: " + VSGameUtilsKt.getAllShips(getLevel()).getById(info.getLastShipStoodOn()).getSlug());
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
                list.add("Static: " + lsship.isStatic());
                list.add("Mass: " + lsship.getInertiaData().getMass() + " kg");
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