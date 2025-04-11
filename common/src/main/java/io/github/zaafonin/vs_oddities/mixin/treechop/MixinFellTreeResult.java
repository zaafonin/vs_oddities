package io.github.zaafonin.vs_oddities.mixin.treechop;

import ht.treechop.api.TreeData;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.Chop;
import ht.treechop.common.chop.FellDataImpl;
import ht.treechop.common.chop.FellTreeResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

import java.util.Collection;

@Pseudo
@Mixin(value = FellTreeResult.class, remap = false)
public abstract class MixinFellTreeResult {
    @Shadow
    @Final
    private Level level;
    @Shadow
    @Final
    private FellDataImpl fellData;

    // This could have been implemented via HT's FellTreeEvent. However, a mixin is loader-agnostic in this case.
    @Inject(
            method = "apply",
            at = @At(
                    value = "INVOKE",
                    target = "Lht/treechop/common/platform/Platform;startFellTreeEvent(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lht/treechop/api/FellData;)Z",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void shipify(BlockPos targetPos, ServerPlayer player, ItemStack tool, CallbackInfo ci) {
        Ship s = VSGameUtilsKt.getShipManagingPos(level, targetPos);
        if (s == null) {
            // This could be accessed like a local but do I really care?
            GameType gameType = player.gameMode.getGameModeForPlayer();

            DenseBlockPosSet blocks = new DenseBlockPosSet();
            TreeData tree = fellData.getTree();
            tree.streamLogs()
                    .filter(pos -> !pos.equals(targetPos) && !player.blockActionRestricted(level, pos, gameType))
                    .filter(pos -> !(level.getBlockState(pos).getBlock() instanceof ChoppedLogBlock)) // Heavily suboptimal.
                    .forEach(pos -> {
                        blocks.add(pos.getX(), pos.getY(), pos.getZ());
                    });
            tree.streamLeaves()
                    .filter(pos -> !player.blockActionRestricted(level, pos, gameType))
                    .forEach(pos -> {
                        blocks.add(pos.getX(), pos.getY(), pos.getZ());
                    });

            ShipAssemblyKt.createNewShipWithBlocks(targetPos, blocks, (ServerLevel) level);
            ci.cancel();
        } else {
            // TODO: Make a config already! Here will be the "tree on ship" counterpart.
        }
    }
}
