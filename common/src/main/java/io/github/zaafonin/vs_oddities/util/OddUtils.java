package io.github.zaafonin.vs_oddities.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3i;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class OddUtils {

    public static ServerLevel getLevelOfShip(MinecraftServer server, Ship ship) {

        return VSGameUtilsKt.getLevelFromDimensionId(server, ship.getChunkClaimDimension());
    }

    public static Stream<BlockPos> streamShipBlocks(MinecraftServer server, Ship ship) {
        ServerLevel level = getLevelOfShip(server, ship);
        AABBic aabb = ship.getShipAABB();
        return IntStream.range(aabb.minX(), aabb.maxX()).mapToObj(
                x -> IntStream.range(aabb.minY(), aabb.maxY()).mapToObj(
                        y -> IntStream.range(aabb.minZ(), aabb.maxZ()).mapToObj(
                                z -> new BlockPos(x, y, z)
                        )
                )
        ).flatMap(Function.identity()).flatMap(Function.identity());
    }

}
