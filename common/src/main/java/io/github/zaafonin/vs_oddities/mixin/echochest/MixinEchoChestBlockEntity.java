package io.github.zaafonin.vs_oddities.mixin.echochest;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fuzs.echochest.world.level.block.entity.EchoChestBlockEntity;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(EchoChestBlockEntity.class)
public class MixinEchoChestBlockEntity {
    @WrapMethod(method = "isOccluded", remap = false)
    private static boolean adjustOcclusionForWorldPosition(Level level, Vec3 pos1, Vec3 pos2, Operation<Boolean> original) {
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
}
