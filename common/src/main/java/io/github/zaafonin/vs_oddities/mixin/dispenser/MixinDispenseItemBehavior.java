package io.github.zaafonin.vs_oddities.mixin.dispenser;

import io.github.zaafonin.vs_oddities.ship.ThrustInducer;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(
        targets = {
                // We need to mixin DispenseItemBehavior.execute, but the method is overridden in various classes, including anonymous ones.
                // Manually specify every class we need. We're good because we're injecting into the same method.

                "net.minecraft.core.dispenser.DispenseItemBehavior$14", // Firework rocket
                "net.minecraft.core.dispenser.DispenseItemBehavior$15",  // Fire charge
                "net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior"  // Arrows, eggs, snowballs, etc.
        }
)
public abstract class MixinDispenseItemBehavior {
    @Inject(method = "execute(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private void addRecoil(BlockSource blockSource, ItemStack itemStack, CallbackInfoReturnable<ItemStack> ci) {
        ServerLevel level = blockSource.getLevel();
        Position position = DispenserBlock.getDispensePosition(blockSource);
        Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);

        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(level, VectorConversionsMCKt.toJOML(position));
        if (ship == null || ship.isStatic()) return; // No ship, no recoil.

        double recoilFactor = 0.5e6; // Small but noticeable recoil for projectiles such as arrows and snowballs.
        // TODO: This should be data!
        if (itemStack.getItem() == Items.FIREWORK_ROCKET)
            recoilFactor = 2e6;
        else if (itemStack.getItem() == Items.FIRE_CHARGE)
            recoilFactor = 5e6;

        if (recoilFactor == 0) return; // Don't apply zero forces.

        Vector3d JOMLposInShip = VectorConversionsMCKt.toJOML(position).sub(ship.getTransform().getPositionInShip());
        Vector3dc JOMLrecoil = VectorConversionsMCKt.toJOMLD(direction.getNormal()).mul(-recoilFactor);

        ThrustInducer applier = ThrustInducer.getOrCreate(ship);
        if (!JOMLrecoil.isFinite()) return; // Something went really wrong with our calculations.

        applier.applyPulse(JOMLrecoil, JOMLposInShip, 1);
    }
}