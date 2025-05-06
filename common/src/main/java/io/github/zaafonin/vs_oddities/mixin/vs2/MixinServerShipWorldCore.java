package io.github.zaafonin.vs_oddities.mixin.vs2;

import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.core.impl.game.ships.DummyShipWorldServer;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;

// Using multiple targets because you can't wrap an interface method.
// Luckily there are just two implementations in vscore, and I really doubt there are any other impls in addons.
@Mixin(value = {
        DummyShipWorldServer.class,
        ShipObjectServerWorld.class
}, remap = false)
public class MixinServerShipWorldCore {
    /*
    @WrapMethod(method = "createNewShipAtBlock(Lorg/joml/Vector3ic;ZDLjava/lang/String;)Lorg/valkyrienskies/core/api/ships/ServerShip;")
    ServerShip createShipObjectImmediately(Vector3ic par1, boolean par2, double par3, String par4, Operation<ServerShip> original) {
        return original.call(
                par1,
                true,
                par3,
                par4
        );
    }
    */
}

