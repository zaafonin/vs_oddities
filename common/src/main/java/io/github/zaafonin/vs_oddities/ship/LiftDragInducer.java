package io.github.zaafonin.vs_oddities.ship;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.util.McMathUtilKt;
import org.valkyrienskies.physics_api.PoseVel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class LiftDragInducer implements ShipForcesInducer {

    double blockEasyLift = 0;
    double blockDrag = 0;
    HashSet<Vector3i> blockLifts = new HashSet<>();

    // TODO: vs_mass-style config.
    static Map<Block, Double> liftValues = Map.of(
            Blocks.GLOWSTONE, 100.0 + 200.0,
            Blocks.END_STONE, 280.0 + 600.0,
            Blocks.CRYING_OBSIDIAN, 235.0 + 1600.0
    );

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        PhysShipImpl implShip = (PhysShipImpl) physShip;
        // Counteract gravity as much as possible.
        // TODO: Support dimension-specific gravity (VS 2.5?)
        double antiGravityForce = implShip.getInertia().getShipMass() * 10.0;
        physShip.applyInvariantForce(new Vector3d(
                0,
                Math.min(antiGravityForce, blockEasyLift * 100.0),
                0
        ));
        // Drag to counteract vertical velocity or at least cushion the fall.
        PoseVel pv = implShip.getPoseVel();
        Vector3dc vel = pv.getVel();
        physShip.applyInvariantForce(new Vector3d(
                0,
                org.joml.Math.clamp(
                        implShip.getInertia().getShipMass() * -vel.y(),
                        blockEasyLift * -100,
                        blockEasyLift * 100
                ),
                0
        ));
        // Even more casual: slightly dampen pitch and roll, you probably don't want your airship to do that.
        // Here easylift blocks don't act to their full force.
        Vector3dc angVel = pv.getOmega();
        physShip.applyInvariantTorque(new Vector3d(
                blockEasyLift * 10.0 * -angVel.x(),
                0,
                blockEasyLift * 10.0 * -angVel.z()
        ));
    }

    public void onSetBlockState(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        double deltaLift = 0;
        deltaLift -= Objects.requireNonNullElse(liftValues.get(oldState.getBlock()), 0.0);
        deltaLift += Objects.requireNonNullElse(liftValues.get(newState.getBlock()), 0.0);
        blockEasyLift += deltaLift;
        if (deltaLift > 0) {
            // Eye candy
            level.sendParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0.0);
        }
        // Modify positional lift (positional upward force, altitude-dependent in the future)
        // TODO: Implement positional lift similarly to Pulse from ThrustInducer.
    }

    public static LiftDragInducer getOrCreate(ServerShip ship) {
        LiftDragInducer result = ship.getAttachment(LiftDragInducer.class);
        if (result == null) {
            result = new LiftDragInducer();
            ship.saveAttachment(LiftDragInducer.class, result);
        }
        return result;
    }
}