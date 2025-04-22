package io.github.zaafonin.vs_oddities.ship;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.List;

public class PhysShipSnooper implements ShipForcesInducer, DebugPresentable {

    public List<ShipForcesInducer> inducerList;
    public double buoyantFactor;

    public static PhysShipSnooper getOrCreate(LoadedServerShip ship) {
        PhysShipSnooper result = ship.getAttachment(PhysShipSnooper.class);
        if (result == null) {
            result = new PhysShipSnooper();
            ship.saveAttachment(PhysShipSnooper.class, result);
        }
        return result;
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        PhysShipImpl impl = (PhysShipImpl)physShip;
        inducerList = impl.getForceInducers();
    }

    @Override
    public void addDebugLines(List<String> lines) {
        /*if (inducerList == null) return;
        lines.add(ChatFormatting.UNDERLINE + "Force Inducers:");
        inducerList.forEach(inducer -> {
            lines.add(ChatFormatting.UNDERLINE + inducer.toString());
            if (!(inducer instanceof PhysShipSnooper) && inducer instanceof DebugPresentable) {
                ((DebugPresentable) inducer).addDebugLines(lines);
            }
            lines.add("");
        });
        lines.add("");*/
    }
}
