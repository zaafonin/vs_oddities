package io.github.zaafonin.vs_oddities.mixin.sculk;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(targets = {
        "net.minecraft.world.level.gameevent.vibrations.VibrationSystem$Listener"
})
public abstract class MixinVibrationSystem {
    @WrapMethod(
            method = "scheduleVibration"
    )
    // GameEvent changed to Holder<GameEvent> in 1.21
    void scheduleVibration(ServerLevel level, VibrationSystem.Data data, GameEvent gameEvent, GameEvent.Context context, Vec3 pos, Vec3 sensorPos, Operation original) {
        Ship sourceShip = VSGameUtilsKt.getShipManagingPos(level, pos);
        Ship sensorShip = VSGameUtilsKt.getShipManagingPos(level, sensorPos);
        if (sourceShip != null) {
            pos = VSGameUtilsKt.toWorldCoordinates(sourceShip, pos);
        }
        if (sensorShip != null) {
            sensorPos = VSGameUtilsKt.toWorldCoordinates(sensorShip, sensorPos);
        }
        original.call(level, data, gameEvent, context, pos, sensorPos);
    }

    @Inject(
            method = "isOccluded",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void skipIfOnDifferentShips(Level level, Vec3 vec3, Vec3 vec32, CallbackInfoReturnable<Boolean> cir) {
        if (VSGameUtilsKt.getShipManagingPos(level, vec3) != VSGameUtilsKt.getShipManagingPos(level, vec32)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    // TODO: Particles are surprisingly non-trivial as they are created from an anonymous class inside a method.
}