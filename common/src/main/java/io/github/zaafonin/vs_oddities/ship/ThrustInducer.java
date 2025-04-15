package io.github.zaafonin.vs_oddities.ship;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class ThrustInducer implements ShipForcesInducer, DebugPresentable {
    // region Structs

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
    }
    // endregion

    // region Stored data
    private final CopyOnWriteArrayList<Pulse> pulses = new CopyOnWriteArrayList<>();
    // endregion

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

    // region External interface
    /**
     * Adds a force that will act for a defined number of physics ticks.
     * <p>
     * TODO: if the ship is static, the pulses will pile up and fire after the ship is unfrozen.
     * If such behavior is undesired, consider checking {@code ship.isStatic()} before applying a pulse.
     * @param pulse A {@link Pulse} containing force vector, application position and duration.
     */
    public void applyPulse(Pulse pulse) {
        pulses.add(pulse);
    }

    /**
     * Adds a force that will act for a defined number of physics ticks.
     * <p>
     * TODO: if the ship is static, the pulses will pile up and fire after the ship is unfrozen.
     * If such behavior is undesired, consider checking {@code ship.isStatic()} before applying a pulse.
     * @param force Vector of force (rotation-dependent, not invariant)
     * @param shipRelativePos Position (in ship coordinates, (0 0 0) for the ship center.
     * @param physTicks Duration to apply the force. 1 game tick ≈ 5 physics ticks.
     * TODO: Ditch the ≈ and adapt to a possibly variable tick rate.
     */
    public void applyPulse(Vector3dc force, Vector3dc shipRelativePos, int physTicks) {
        pulses.add(new Pulse(force, shipRelativePos, physTicks));
    }
    // endregion

    // Boilerplate as seen in Tournament, Kontraption, etc.
    public static ThrustInducer getOrCreate(ServerShip ship) {
        ThrustInducer result = ship.getAttachment(ThrustInducer.class);
        if (result == null) {
            result = new ThrustInducer();
            ship.saveAttachment(ThrustInducer.class, result);
        }
        return result;
    }

    @Override
    public void addDebugLines(List<String> lines) {
        lines.add("Active Pulses: " + pulses.size());
    }
}
