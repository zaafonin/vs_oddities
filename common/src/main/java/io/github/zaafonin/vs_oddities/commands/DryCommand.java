package io.github.zaafonin.vs_oddities.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public class DryCommand {
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
            (object, object2) -> Component.translatable("commands.fill.toobig", object, object2)
    );
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(
                Commands.literal("dry")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .then(
                                Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(
                                                Commands.argument("to", BlockPosArgument.blockPos())
                                                        .executes(commandContext -> dryBlocks(
                                                                commandContext.getSource(),
                                                                BoundingBox.fromCorners(
                                                                        BlockPosArgument.getLoadedBlockPos(commandContext, "from"),
                                                                        BlockPosArgument.getLoadedBlockPos(commandContext, "to")
                                                                ))))));
    }

    public static int dryBlocks(
            CommandSourceStack commandSourceStack, BoundingBox boundingBox
    ) throws CommandSyntaxException {
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        int j = commandSourceStack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
        if (i > j) {
            throw ERROR_AREA_TOO_LARGE.create(j, i);
        } else {
            List<BlockPos> list = Lists.<BlockPos>newArrayList();
            ServerLevel serverLevel = commandSourceStack.getLevel();

            for (BlockPos blockPos : BlockPos.betweenClosed(
                    boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()
            )) {
                BlockState bs = serverLevel.getBlockState(blockPos);
                if (bs.liquid()) {
                    serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                } else {
                    // Kludge to include seagrass
                    if (bs.getBlock() instanceof LiquidBlockContainer && !(bs.getBlock() instanceof SimpleWaterloggedBlock)) {
                        serverLevel.destroyBlock(blockPos, true);
                        serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        serverLevel.setBlock(blockPos, bs.trySetValue(BlockStateProperties.WATERLOGGED, false), 2);
                    }
                }
            }
        }
        return 1;
    }
}
