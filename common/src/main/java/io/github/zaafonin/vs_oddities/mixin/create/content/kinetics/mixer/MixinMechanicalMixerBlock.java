package io.github.zaafonin.vs_oddities.mixin.create.content.kinetics.mixer;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(MechanicalMixerBlock.class)
public abstract class MixinMechanicalMixerBlock extends KineticBlock {
    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        // Basin mixin tries to raycast a mechanical mixer and fails. Basins themselves have a shape just for raycasts.
        return Shapes.block();
    }

    // Dummy
    public MixinMechanicalMixerBlock(Properties properties) {
        super(properties);
    }
}
