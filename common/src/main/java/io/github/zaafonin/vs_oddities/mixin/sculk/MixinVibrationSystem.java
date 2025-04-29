package io.github.zaafonin.vs_oddities.mixin.sculk;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
    void scheduleVibration(ServerLevel level, VibrationSystem.Data data, GameEvent gameEvent, GameEvent.Context context, Vec3 pos, Vec3 sensorPos, Operation original) { // GameEvent changed to Holder<GameEvent> in 1.21
        if (!VSOdditiesConfig.Common.SHIP_AWARE_SCULK.get()) {
            original.call(level, data, gameEvent, context, pos, sensorPos);
            return;
        }
        original.call(level, data, gameEvent, context,
                    OddUtils.getWorldCoordinates(level, pos),
                    OddUtils.getWorldCoordinates(level, sensorPos)
        );
    }

    @WrapMethod(method = "isOccluded")
    private static boolean adjustOcclusionForWorldPosition(Level level, Vec3 pos1, Vec3 pos2, Operation<Boolean> original) {
        if (!VSOdditiesConfig.Common.SHIP_AWARE_SCULK.get()) {
            return original.call(level, pos1, pos2);
        }

        if (VSGameUtilsKt.getShipManagingPos(level, pos1) != VSGameUtilsKt.getShipManagingPos(level, pos2)) {
            // In ship-to-world or ship-to-ship events, check occlusion in world coordinates.
            return original.call(level,
                    OddUtils.getWorldCoordinates(level, pos1),
                    OddUtils.getWorldCoordinates(level, pos2)
            );
        }
        // Use original arguments when both positions are in world or on the same ship.
        return original.call(level, pos1, pos2);
    }

    // TODO: Particles are surprisingly non-trivial as they are created from an anonymous class inside a method.
}