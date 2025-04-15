package io.github.zaafonin.vs_oddities.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class OddUtils {

    public static ServerLevel getLevelOfShip(MinecraftServer server, Ship ship) {
        return VSGameUtilsKt.getLevelFromDimensionId(server, ship.getChunkClaimDimension());
    }

    /**
     * Get all blocks of a {@link Ship} as a Java stream.
     * Limited by ship's {@link org.joml.primitives.AABBi}.
     * @return {@link Stream<BlockPos>} with positions laid out sequentially: (0, 0, 0), (0, 0, 1), ..., (0, 0, z), (0, 1, 0), (0, 1, 1), ...
     */
    public static Stream<BlockPos> streamShipBlocks(Level level, Ship ship) {
        AABBic aabb = ship.getShipAABB();
        return IntStream.range(aabb.minX(), aabb.maxX()).mapToObj(
                x -> IntStream.range(aabb.minY(), aabb.maxY()).mapToObj(
                        y -> IntStream.range(aabb.minZ(), aabb.maxZ()).mapToObj(
                                z -> new BlockPos(x, y, z)
                        )
                )
        ).flatMap(Function.identity()).flatMap(Function.identity());
    }

    /**
     * Get an iterable of all ship blocks. Useful for filling and other per-block operations.
     * <p>
     * Can be iterated like: {@code for (BlockPos blockPos : OddUtils.iterateShipBlocks(level, ship)) { ... } }
     */
    public static Iterable<BlockPos> iterateShipBlocks(Level level, Ship ship) {
        AABBic aabb = ship.getShipAABB();
        return BlockPos.betweenClosed(
                aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ()
        );
    }

    /**
     * A less verbose wrapper around {@link VSGameUtilsKt#getWorldCoordinates}.
     * @return {@link Vec3} pos in world coordinates
     */
    public static Vec3 getWorldCoordinates(Level level, Vec3 pos) {
        return VectorConversionsMCKt.toMinecraft(VSGameUtilsKt.getWorldCoordinates(level, BlockPos.containing(pos), VectorConversionsMCKt.toJOML(pos)));
    }

    /**
     * A null-checking wrapper around {@link VSGameUtilsKt#toWorldCoordinates(Ship, Vec3)}.
     * If {@link Ship} is null, return the position unmodified.
     * @return {@link Vec3} pos in world coordinates
     */
    public static Vec3 toWorldCoordinates(Ship ship, Vec3 pos) {
        if (ship == null) return pos;
        return VSGameUtilsKt.toWorldCoordinates(ship, pos);
    }

    /**
     * Get nearest {@link BlockPos} for a point by rounding.
     * Differs from {@link BlockPos#containing(Position)} which floors coordinates.
     * <p>
     * NB: Does not account for nearby ships: nearest block to a regular world position is always in world, nearest block to a position in shipyard is always in the same shipyard.
     * @return resulting blockPos
     */
    public static BlockPos toNearestBlock(Vec3 vec) {
        return new BlockPos((int) (vec.x + 0.5), (int) (vec.y + 0.5), (int) (vec.z + 0.5));
    }

    /**
     * Get nearest {@link BlockPos} for a point by rounding. See {@link OddUtils#toNearestBlock(Vec3)}
     * @return resulting blockPos
     */
    public static BlockPos toNearestBlock(Vector3dc vec) {
        return new BlockPos((int) (vec.x() + 0.5), (int) (vec.y() + 0.5), (int) (vec.z() + 0.5));
    }

    /**
     * Convert a {@link AABBic} (integer AABB from JOML) to a Minecraft {@link BoundingBox}.
     */
    public static BoundingBox toMinecraft(AABBic aabb) {
        return new BoundingBox(
                aabb.minX(), aabb.minY(), aabb.minZ(),
                aabb.maxX(), aabb.maxY(), aabb.maxZ()
        );
    }
}
