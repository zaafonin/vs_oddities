package io.github.zaafonin.vs_oddities.mixin.biomes;

import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.ship.OddAttachment;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.joml.Vector3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess {

    @Shadow
    public abstract ChunkPos getPos();

    @Final
    @Shadow
    protected LevelHeightAccessor levelHeightAccessor;

    @Shadow
    // A proper good behaving LevelHeightAccessor that you can't use as a Level.
    public abstract LevelHeightAccessor getHeightAccessorForGeneration();

    @Shadow
    public abstract LevelChunkSection getSection(int sectionIndex);

    @Inject(method = "fillBiomesFromNoise", at = @At("HEAD"), cancellable = true)
    protected void adjustForShip(BiomeResolver resolver, Climate.Sampler sampler, CallbackInfo ci) {
        if (!VSOdditiesConfig.Common.FIX_BIOME_MISMATCH.get()) return;

        // HACK: This shouldn't work. We only get a "Level Height Accessor" but luckily it's a real Level.
        // At least real enough for VS2 methods to work on.
        ServerLevel level = (ServerLevel) levelHeightAccessor;
        if (level == null) return;

        ChunkPos chunkpos = this.getPos();
        // Using LoadedServerShip here (like one is supposed to when working with attachments) creates a regression.
        // Possible cause: it's called on newly created ships that are not "loaded" yet. Remedy: use ServerShip.
        ServerShip ship = VSGameUtilsKt.getShipManagingPos(level, chunkpos);
        if (ship == null) return; // Not on a ship, filling biomes in the regular way.

        Vector3i shipyardBase = ship.getChunkClaim().getCenterBlockCoordinates(VSGameUtilsKt.getYRange(level), new Vector3i());
        OddAttachment oddAttachment = OddAttachment.getOrCreate(ship);
        if (oddAttachment == null) return; // Should not happen!

        // Copy-paste original method, but calculate biomes at the creation position instead of the shipyard.
        // TODO: As evident from slightly wrong grass gradients, the x and z are inaccurate by not more than a chunk.
        int x = QuartPos.fromBlock((chunkpos.getMinBlockX() - shipyardBase.x) + oddAttachment.getOriginalChunkPos().getMinBlockX());
        int z = QuartPos.fromBlock((chunkpos.getMinBlockZ() - shipyardBase.z) + oddAttachment.getOriginalChunkPos().getMinBlockZ());
        LevelHeightAccessor realLevelheightaccessor = this.getHeightAccessorForGeneration();

        for (int k = realLevelheightaccessor.getMinSection(); k < realLevelheightaccessor.getMaxSection(); ++k) {
            LevelChunkSection levelchunksection = this.getSection(realLevelheightaccessor.getSectionIndexFromSectionY(k));
            int l = QuartPos.fromSection(k);
            levelchunksection.fillBiomesFromNoise(resolver, sampler, x, l, z);
        }
        ci.cancel();
    }
}