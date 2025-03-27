package io.github.zaafonin.vs_oddities.ship;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class OddAttachment {
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
    public OddAttachment() {}
}
