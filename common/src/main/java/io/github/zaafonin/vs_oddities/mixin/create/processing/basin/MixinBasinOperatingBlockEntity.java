package io.github.zaafonin.vs_oddities.mixin.create.processing.basin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

import java.util.Optional;

@Pseudo
@Mixin(BasinOperatingBlockEntity.class)
public abstract class MixinBasinOperatingBlockEntity extends BlockEntity {
    @Inject(method = "getBasin", at = @At("RETURN"), cancellable = true, remap = false)
    protected void checkShipBasins(CallbackInfoReturnable<Optional<BasinBlockEntity>> cir) {
        if (!VSOdditiesConfig.Common.SHIP_AWARE_CREATE_PROCESSING.get()) return;

        if (cir.getReturnValue().isEmpty()) {
            // If we didn't find a basin, account for ship positions by raycasting (duh).
            // TODO: Remember position (and ship) of the found basin and only check if it's still below us.
            // In theory transforming a couple of vectors will be more performant than a raycast to ships.
            Vec3 mixerWorldPos = OddUtils.getWorldCoordinates(level, worldPosition.getCenter());
            BlockHitResult hit = RaycastUtilsKt.clipIncludeShips(level, new ClipContext(
                    mixerWorldPos,
                    mixerWorldPos.subtract(0, 2.5, 0), // ~ worldPosition.below(2). 2.5 because basins are not solid.
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null
            ));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockEntity basinBE = level.getBlockEntity(hit.getBlockPos());
                cir.setReturnValue(!(basinBE instanceof BasinBlockEntity) ? Optional.empty() : Optional.of((BasinBlockEntity)basinBE));
                cir.cancel();
            }
        }
    }

    // Dummy
    public MixinBasinOperatingBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }
}
