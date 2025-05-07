package io.github.zaafonin.vs_oddities.ship;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        public Vector3dc torque;
        public Vector3dc pos;
        public int ticks;
        public boolean invariant;

        public Pulse(Vector3dc force, Vector3dc torque, Vector3dc shipRelativePos, int ticks, boolean invariant) {
            this.force = force;
            this.torque = torque;
            this.pos = shipRelativePos;
            this.ticks = ticks;
            this.invariant = invariant;
        }

        public Pulse(Vector3dc force, Vector3dc torque, Vector3dc shipRelativePos, boolean invariant) {
            this(force, torque, shipRelativePos, 1, invariant);
        }
    }
    // endregion

    // region Stored data
    @JsonIgnore
    private final ConcurrentLinkedQueue<Pulse> pulses = new ConcurrentLinkedQueue<>();
    // endregion

    // VS2 necessary
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        // Timed pulses
        pulses.forEach(pulse -> {
            if (pulse.invariant) {
                if (pulse.force != null) physShip.applyInvariantForceToPos(pulse.force, pulse.pos);
                if (pulse.torque != null) physShip.applyInvariantTorque(pulse.torque);
            } else {
                if (pulse.force != null) physShip.applyRotDependentForceToPos(pulse.force, pulse.pos);
                if (pulse.torque != null) physShip.applyRotDependentTorque(pulse.torque);
            }

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
    public void applyPulse(Vector3dc force, Vector3dc torque, Vector3dc shipRelativePos, int physTicks, boolean invariant) {
        pulses.add(new Pulse(force, torque, shipRelativePos, physTicks, invariant));
    }
    // endregion

    // Boilerplate as seen in Tournament, Kontraption, etc.
    public static ThrustInducer getOrCreate(LoadedServerShip ship) {
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
