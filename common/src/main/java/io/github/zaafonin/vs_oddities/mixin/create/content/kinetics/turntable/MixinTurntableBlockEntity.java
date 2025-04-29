package io.github.zaafonin.vs_oddities.mixin.create.content.kinetics.turntable;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.kinetics.turntable.TurntableBlockEntity;
import ht.treechop.server.Server;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.ship.ThrustInducer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

import java.util.Iterator;

@Pseudo
@Mixin(TurntableBlockEntity.class)
public abstract class MixinTurntableBlockEntity extends KineticBlockEntity {
    @Override
    public void tick() {
        if (!VSOdditiesConfig.Common.SHIP_AWARE_CREATE_TURNTABLES_ROTATE_SHIPS.get()) return;

        // Basin mixin tries to raycast a mechanical mixer and fails. Basins themselves have a shape just for raycasts.
        super.tick();
        if (level.isClientSide) return;

        BlockHitResult hit = RaycastUtilsKt.clipIncludeShips(level, new ClipContext(
                        worldPosition.getCenter(),
                        worldPosition.getCenter().add(0, 0.05, 0),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        null
                ));
        if (hit.getType() == HitResult.Type.BLOCK) {
            LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel)level, hit.getBlockPos());
            if (ship != null) {
                Vector3dc rotationPos = ship.getWorldToShip().transformPosition(VectorConversionsMCKt.toJOML(worldPosition.getCenter()));
                ThrustInducer inducer = ThrustInducer.getOrCreate(ship);
                double shipAngVel = ship.getOmega().y();
                double targetAngVel = getSpeed() / 60 * 2 * Math.PI /* BS factor */ * 2;
                double force = ship.getInertiaData().getMass() * (targetAngVel - shipAngVel);
                inducer.applyPulse(null, new Vector3d(0, force, 0), rotationPos, 3, true);
            }
        }
    }

    // Dummy
    public MixinTurntableBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }
}
