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
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
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
            Blocks.GLOWSTONE, 200.0,
            Blocks.END_STONE, 600.0,
            Blocks.CRYING_OBSIDIAN, 1500.0
    );

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        PhysShipImpl implShip = (PhysShipImpl) physShip;
        // TODO: This is a placeholder. Anyone got a spare PID regulator?
        physShip.applyInvariantForce(new Vector3d(0, blockEasyLift * 100 /* because dt = 1/100 */, 0));
    }

    public void onSetBlockState(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        double deltaLift = 0;
        deltaLift -= Objects.requireNonNullElse(liftValues.get(oldState.getBlock()), 0.0);
        deltaLift += Objects.requireNonNullElse(liftValues.get(newState.getBlock()), 0.0);
        blockEasyLift += deltaLift;
        // Eye candy
        if (deltaLift > 0) {
            level.sendParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.5, 0.5, 0.5, 0.0);
        }
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