package io.github.zaafonin.vs_oddities.mixin.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
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
    abstract LiteralArgumentBuilder<VSCommandSource> literal(String name);

    @Shadow
    abstract <T> RequiredArgumentBuilder argument(String name, ArgumentType<T> type);

    @Inject(method = "registerServerCommands", at = @At("TAIL"), remap = false)
    private void modifyvs(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        System.out.println("COMMAND PATH " + dispatcher.getPath(dispatcher.getRoot()));
        dispatcher.register(
                literal("vs").then(literal("break").then(argument("ships", ShipArgument.Companion.ships()).then(argument("drop", BoolArgumentType.bool())).executes(ctx ->
                        {
                            MinecraftServer server = ((CommandContext<CommandSourceStack>)ctx).getSource().getServer();
                            Set<ServerShip> ships = ShipArgument.Companion.getShips(ctx, "ships");
                            Boolean drop = BoolArgumentType.getBool(ctx, "drop");
                            ships.forEach(ship ->
                                    {
                                        ServerLevel level = OddUtils.getLevelOfShip(server, ship);
                                        OddUtils.streamShipBlocks(server, ship).forEach(
                                                pos -> level.destroyBlock(pos, drop)
                                        );
                                    }
                            );
                            return 0;
                        }))));
    }
}
