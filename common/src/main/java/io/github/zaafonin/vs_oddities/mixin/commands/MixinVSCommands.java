package io.github.zaafonin.vs_oddities.mixin.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.zaafonin.vs_oddities.ship.ThrustApplier;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.primitives.AABBi;
import org.joml.primitives.AABBic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.core.api.world.ShipWorld;
import org.valkyrienskies.core.apigame.VSCore;
import org.valkyrienskies.core.impl.game.ships.ShipData;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.command.ShipArgument;
import org.valkyrienskies.mod.common.command.VSCommands;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.mixinducks.feature.command.VSCommandSource;
import io.github.zaafonin.vs_oddities.util.OddUtils;

import java.util.*;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

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
