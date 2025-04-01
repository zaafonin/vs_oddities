package io.github.zaafonin.vs_oddities.mixin.biomes;

import io.github.zaafonin.vs_oddities.ship.OddAttachment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.GameServer;
import org.valkyrienskies.core.impl.game.ships.ShipData;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

// Kludge for assigning shipyard biome so that a ship created in plains doesn't become a mushroom island.
@Mixin(value = ShipObjectServerWorld.class, remap = false)
public class MixinShipObjectServerWorld {
    @Shadow
    private GameServer gameServer;

    @Inject(method = "createNewShipAtBlock", at = @At("RETURN"))
    private void modifyBiomesOfShipyard(Vector3ic blockPosInWorldCoordinates, boolean createShipObjectImmediately, double scaling, String dimensionId, CallbackInfoReturnable<ShipData> cir) {
        MinecraftServer server = (MinecraftServer) gameServer;
        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, dimensionId);

        ServerShip serverShip = (ServerShip)cir.getReturnValue();
        if (serverShip.getAttachment(OddAttachment.class) == null) {
            serverShip.saveAttachment(
                    OddAttachment.class,
                    new OddAttachment(serverShip.getChunkClaimDimension(), VectorConversionsMCKt.toBlockPos(blockPosInWorldCoordinates))
            );
        }
    }
}
