package io.github.zaafonin.vs_oddities.ship;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.CopyOnWriteArrayList;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class ThrustInducer implements ShipForcesInducer {
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
    public static ThrustInducer getOrCreate(ServerShip ship) {
        ThrustInducer result = ship.getAttachment(ThrustInducer.class);
        if (result == null) {
            result = new ThrustInducer();
            ship.saveAttachment(ThrustInducer.class, result);
        }
        return result;
    }
}
