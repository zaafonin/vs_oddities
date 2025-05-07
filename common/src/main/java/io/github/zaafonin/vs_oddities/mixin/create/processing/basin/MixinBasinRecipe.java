package io.github.zaafonin.vs_oddities.mixin.create.processing.basin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

@Pseudo
@Mixin(BasinRecipe.class)
public abstract class MixinBasinRecipe extends ProcessingRecipe<SmartInventory> {
    @Inject(
            method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"
            )
    )
    private static void raycastHeatSource(
            BasinBlockEntity basin, Recipe<?> recipe, boolean test, CallbackInfoReturnable<Boolean> cir,
            @Local LocalRef<BlazeBurnerBlock.HeatLevel> heat
    ) {
        if (!VSOdditiesConfig.Common.SHIP_AWARE_CREATE_PROCESSING.get()) return;

        if (heat.get() == BlazeBurnerBlock.HeatLevel.NONE) {
            // Perform the raycast.
            Level level = basin.getLevel();
            Vec3 mixerWorldPos = OddUtils.getWorldCoordinates(level, basin.getBlockPos().getCenter());
            BlockHitResult hit = RaycastUtilsKt.clipIncludeShips(level, new ClipContext(
                    mixerWorldPos.subtract(0, 1, 0),
                    mixerWorldPos.subtract(0, 2, 0), // ~ worldPosition.below(2). 2.5 because basins are not solid.
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null
            ));
            if (hit.getType() == HitResult.Type.BLOCK) {
                heat.set(BasinBlockEntity.getHeatLevelOf(level.getBlockState(hit.getBlockPos())));
            }
        }
    }

    // Dummy
    public MixinBasinRecipe(IRecipeTypeInfo typeInfo, ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(typeInfo, params);
    }
}
