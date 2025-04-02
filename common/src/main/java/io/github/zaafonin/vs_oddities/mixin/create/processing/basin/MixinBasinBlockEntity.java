package io.github.zaafonin.vs_oddities.mixin.create.processing.basin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;
import org.valkyrienskies.mod.common.command.VSCommands;

import java.util.Optional;

@Pseudo
@Mixin(BasinBlockEntity.class)
public abstract class MixinBasinBlockEntity extends SmartBlockEntity {
    @Shadow public abstract void notifyUpdate();

    @Shadow public abstract void notifyChangeOfContents();

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("RETURN"), remap = false)
    protected void tickMoreOften(BlockEntityType type, BlockPos pos, BlockState state, CallbackInfo ci) {
        setLazyTickRate(3); // Update every second.
    }

    @Inject(method = "lazyTick", at = @At("RETURN"), remap = false)
    protected void forceUpdate(CallbackInfo ci) {
        if (!level.isClientSide) {
            notifyUpdate();
            notifyChangeOfContents();
        }
    }

    @Inject(method = "getOperator", at = @At("RETURN"), cancellable = true, remap = false)
    protected void checkShipBasinCheckers(CallbackInfoReturnable<Optional<BasinOperatingBlockEntity>> cir) {
        if (cir.getReturnValue().isEmpty()) {
            // If we didn't find a basin operator, account for ship positions by raycasting (duh).
            Ship ownShip = VSGameUtilsKt.getShipManagingPos(level, worldPosition);
            Vec3 basinWorldPos = OddUtils.getWorldCoordinates(level, worldPosition.getCenter());
            BlockHitResult hit = RaycastUtilsKt.clipIncludeShips(level, new ClipContext(
                    basinWorldPos,
                    basinWorldPos.add(0, 2.5, 0),
                    ClipContext.Block.VISUAL, // Stupid Create giving mixers a complex VoxelShape that depends on who's asking.
                    ClipContext.Fluid.NONE,
                    null
            ), false, (ownShip != null ? ownShip.getId() : null));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockEntity basinOperatorBE = level.getBlockEntity(hit.getBlockPos());
                cir.setReturnValue(!(basinOperatorBE instanceof BasinOperatingBlockEntity) ? Optional.empty() : Optional.of((BasinOperatingBlockEntity)basinOperatorBE));
                cir.cancel();
            }
        }
    }

    // Dummy
    public MixinBasinBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }
}
