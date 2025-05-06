package io.github.zaafonin.vs_oddities.mixin.vs2;

import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.game.ships.QueryableShipDataImpl;
import org.valkyrienskies.core.impl.game.ships.ShipData;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServer;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.hooks.VSEvents;
import org.valkyrienskies.core.impl.networking.NetworkChannel;

import java.util.List;

@Mixin(value = ShipObjectServerWorld.class, remap = false)
public class MixinShipObjectServerWorld {
    @Shadow
    @Final
    private QueryableShipDataImpl<Ship> _loadedShips;
    @Shadow
    @Final
    private List<Ship> newShipObjects;

    // This fires when an exception is created, which happens when createShipObjectImmediately is true.
    // Not useful because created PhysShips do not have any inertia. Will try to fix it later.
    @Inject(
            method = "createNewShipAtBlock(Lorg/joml/Vector3ic;ZDLjava/lang/String;)Lorg/valkyrienskies/core/impl/game/ships/ShipData;",
            at = @At(
                    value = "NEW",
                    target = "kotlin/NotImplementedError"
            ),
            cancellable = true
    )
    void createImmediately(
            Vector3ic blockPosInWorldCoordinates, boolean createShipObjectImmediately, double scaling, String dimensionId, CallbackInfoReturnable<ShipData> cir,
            @Local ShipData ship
    ) {
        long shipId = ship.getId();
        if (ship.getInertiaData().getMass() == (double)0.0F) {
            NetworkChannel.Companion.getLogger().warn("Ship with ID " + shipId + " has a mass of 0.0, not creating a ShipObject");
        } else if (!this._loadedShips.contains(shipId)) {
            NetworkChannel.Companion.getLogger().info("You should create a ShipObject NOW!");
            ShipObjectServer shipObjectServer = new ShipObjectServer(ship);
            this.newShipObjects.add(ship);
            this._loadedShips.addShipData(ship);
            VSEvents.INSTANCE.getShipLoadEvent().emit(new VSEvents.ShipLoadEvent(shipObjectServer));
        }
        cir.cancel();
    }
}
