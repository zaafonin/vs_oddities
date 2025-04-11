package io.github.zaafonin.vs_oddities.mixin.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zaafonin.vs_oddities.commands.DryCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.command.ShipArgument;
import org.valkyrienskies.mod.common.command.VSCommands;
import org.valkyrienskies.mod.mixinducks.feature.command.VSCommandSource;
import io.github.zaafonin.vs_oddities.util.OddUtils;

import java.util.*;

@Mixin(VSCommands.class)
public abstract class MixinVSCommands {
    @Shadow
    protected abstract LiteralArgumentBuilder<VSCommandSource> literal(String name);

    @Shadow
    protected abstract <T> RequiredArgumentBuilder argument(String name, ArgumentType<T> type);

    @Inject(method = "registerServerCommands", at = @At("TAIL"), remap = false)
    private void modifyvs(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        // TODO: There might a better place to register a command that is not exactly VS-related.
        DryCommand.register(dispatcher);
        // TODO: This used to work a few days ago. Not first priority but it would be good to get it functional.
        dispatcher.register(
                literal("vs").then(literal("break").then(argument("ships", ShipArgument.Companion.ships()).then(argument("drop", BoolArgumentType.bool()).executes(ctx ->
                {
                    MinecraftServer server = ((CommandContext<CommandSourceStack>) ctx).getSource().getServer();
                    Set<ServerShip> ships = ShipArgument.Companion.getShips(ctx, "ships");
                    boolean drop = BoolArgumentType.getBool(ctx, "drop");
                    ships.forEach(ship ->
                            {
                                ServerLevel level = OddUtils.getLevelOfShip(server, ship);
                                for (BlockPos blockPos : OddUtils.iterateShipBlocks(level, ship)) {
                                    level.destroyBlock(blockPos, drop);
                                }
                            }
                    );
                    return 0;
                })))));
        dispatcher.register(
                literal("vs").then(literal("dry").then(argument("ships", ShipArgument.Companion.ships()).executes(ctx ->
                {
                    MinecraftServer server = ((CommandContext<CommandSourceStack>) ctx).getSource().getServer();
                    Set<ServerShip> ships = ShipArgument.Companion.getShips(ctx, "ships");
                    ships.forEach(ship ->
                    {
                        ServerLevel level = OddUtils.getLevelOfShip(server, ship);
                        try {
                            DryCommand.dryBlocks((CommandSourceStack) ctx.getSource(), OddUtils.toMinecraft(ship.getShipAABB()));
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return 0;
                }))));
    }
}
