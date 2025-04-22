package io.github.zaafonin.vs_oddities.ship;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Debug;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.List;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class OddAttachment implements ShipForcesInducer, DebugPresentable {
    String origDim;
    // Why oh why can't we serialize vanilla Minecraft vectors? BlockPos, ChunkPos, etc.
    Vector3i origWorldPos;
    Vector2i origChunkPos;

    public String getOriginalDim() {
        return origDim;
    }

    public BlockPos getOriginalBlockPos() {
        return VectorConversionsMCKt.toBlockPos(origWorldPos);
    }

    public ChunkPos getOriginalChunkPos() {
        return VectorConversionsMCKt.toMinecraft(origChunkPos);
    }

    public OddAttachment(String dim, BlockPos blockPos, ChunkPos chunkPos) {
        origDim = dim;
        origWorldPos = VectorConversionsMCKt.toJOML(blockPos);
        origChunkPos = VectorConversionsMCKt.toJOML(chunkPos);
    }

    public OddAttachment(String dim, BlockPos blockPos) {
        origDim = dim;
        origWorldPos = VectorConversionsMCKt.toJOML(blockPos);
        origChunkPos = VectorConversionsMCKt.toJOML(new ChunkPos(blockPos));
    }

    public OddAttachment(String dim, Vector3i blockCoords, Vector2i chunkCoords) {
        origDim = dim;
        origWorldPos = blockCoords;
        origChunkPos = chunkCoords;
    }

    public OddAttachment() {
    }

    public static OddAttachment getOrCreate(LoadedServerShip ship) {
        OddAttachment result = ship.getAttachment(OddAttachment.class);
        if (result == null) {
            result = new OddAttachment();
            ship.saveAttachment(OddAttachment.class, result);
        }
        return result;
    }

    @Override
    public void addDebugLines(List<String> lines) {
        lines.add("Shipified In: " + origDim);
        lines.add(String.format("Shipified At: %d, %d, %d", origWorldPos.x, origWorldPos.y, origWorldPos.z));
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {

    }
}
