package io.github.zaafonin.vs_oddities.mixin.mass;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.mod.common.DefaultBlockStateInfoProvider;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(DefaultBlockStateInfoProvider.class)
public class MixinDefaultBlockStateInfoProvider {
    @WrapMethod(method = "getBlockStateMass", remap = false)
    private Double adjustMassForShape(BlockState blockState, Operation<Double> original) {
        double ton = original.call(blockState);

        if (!VSOdditiesConfig.Common.FIX_DEFAULT_MASS.get()) return ton;

        if (ton == 0) {
            // Skip the overhead for air blocks.
            return 0.0;
        }
        try {
            final double[] volume = {0}; // Using an array to allow modification in lambda.
            blockState.getCollisionShape(null, null).forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
                volume[0] += Math.abs((x2 - x1) * (y2 - y1) * (z2 - z1));
            });
            return Math.max(0.5, ((double)Math.round(ton * volume[0] * 2)) / 2); // Round to 0.5 kg and handle zero.
        } catch (Exception e) {
            // There might be reasons for this to fail, as we are passing nulls to getCollisionShape.
            // These are out of our control. Defaulting to VS2 behavior.
            return ton;
        }
    }
}
