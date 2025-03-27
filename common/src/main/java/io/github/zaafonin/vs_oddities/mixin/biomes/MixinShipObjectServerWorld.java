package io.github.zaafonin.vs_oddities.mixin.biomes;
import io.github.zaafonin.vs_oddities.ship.OddAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet;
import org.valkyrienskies.core.apigame.world.properties.DimensionIdKt;
import org.valkyrienskies.core.impl.game.ships.ShipData;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.api.ships.properties.ChunkClaim;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.apigame.GameServer;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Kludge for assigning shipyard biome so that a ship created in plains doesn't become a mushroom island.
@Mixin(value = ShipObjectServerWorld.class, remap = false)
public class MixinShipObjectServerWorld {
    @Shadow
    private GameServer gameServer;

    @Inject(method = "createNewShipAtBlock", at = @At("RETURN"))
    private void modifyBiomesOfShipyard(Vector3ic blockPosInWorldCoordinates, boolean createShipObjectImmediately, double scaling, String dimensionId, CallbackInfoReturnable<ShipData> cir) {
        MinecraftServer server = (MinecraftServer)gameServer;
        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, dimensionId);

        ShipData ship = cir.getReturnValue();
        ServerShip serverShip = ship;
        serverShip.saveAttachment(OddAttachment.class, new OddAttachment(serverShip.getChunkClaimDimension(), VectorConversionsMCKt.toBlockPos(blockPosInWorldCoordinates)));
    }
}
