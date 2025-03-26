package io.github.zaafonin.vs_oddities.ship;

import ht.treechop.server.Server;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.api.world.ShipWorld;
import org.valkyrienskies.core.apigame.world.properties.DimensionIdKt;
import org.valkyrienskies.core.util.VSCoreUtilKt;
import org.valkyrienskies.mod.util.McMathUtilKt;

import java.util.concurrent.CopyOnWriteArrayList;

public class ThrustApplier implements ShipForcesInducer {
    // Structs
    public class Pulse {
        public Vector3dc force;
        public Vector3dc pos;
        public int ticks;

        public Pulse(Vector3dc force, Vector3dc shipRelativePos, int ticks) {
            this.force = force;
            this.pos = shipRelativePos;
            this.ticks = ticks;
        }
        public Pulse(Vector3dc force, Vector3dc shipRelativePos) {
            this(force, shipRelativePos, 1);
        }
    };

    // Stored data
    private final CopyOnWriteArrayList<Pulse> pulses = new CopyOnWriteArrayList<>();

    // VS2 necessary
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        // Timed pulses
        pulses.forEach(pulse -> {
            physShip.applyRotDependentForceToPos(pulse.force, pulse.pos);

            pulse.ticks -= 1;
        });
        pulses.removeIf(pulse -> pulse.ticks <= 0);
    }

    // Mod interface
    public void applyPulse(Pulse pulse) {
        pulses.add(pulse);
    }
    public void applyPulse(Vector3dc force, Vector3dc pos, int ticks) {
        pulses.add(new Pulse(force, pos, ticks));
    }

    // Boilerplate as seen in Tournament, Kontraption, etc.
    public static ThrustApplier getOrCreate(ServerShip ship) {
        ThrustApplier result = ship.getAttachment(ThrustApplier.class);
        if (result == null) {
            result = new ThrustApplier();
            ship.saveAttachment(ThrustApplier.class, result);
        }
        return result;
    }
}
