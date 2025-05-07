package io.github.zaafonin.vs_oddities.mixin.clockwork;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.clockwork.content.curiosities.tools.wanderwand.WanderWandItem;

@Mixin(value = WanderWandItem.class, remap = false)
public class MixinWanderWandItem extends Item {
    @Unique
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        HitResult result = Minecraft.getInstance().hitResult;
        if (result.getType() != HitResult.Type.BLOCK) {
            float pickDistance = 3;
            // Code from AngelBlockRenewed by LaidBackSloth.
            double x = player.getX() + player.getLookAngle().x * pickDistance;
            double y = player.getEyeY() + player.getLookAngle().y * pickDistance;
            double z = player.getZ() + player.getLookAngle().z * pickDistance;
            BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));

            // Fake a hit result.
            BlockHitResult fakeResult = new BlockHitResult(
                    player.position(),
                    Direction.UP, // TODO: proper direction maybe?
                    pos,
                    true
            );

            UseOnContext useOnContext = new UseOnContext(player, InteractionHand.MAIN_HAND, fakeResult);
            useOn(useOnContext);
        }
        return super.use(level, player, hand);
    }

    // Dummy
    public MixinWanderWandItem(Properties properties) {
        super(properties);
    }

    /*
    @Override
    public InteractionResultHolder<ItemStack> use(Level arg, Player arg2, InteractionHand arg3) {

        ItemStack itemstack = arg2.getItemInHand(arg3);
        if (itemstack.isEdible()) {
            if (arg2.canEat(itemstack.getFoodProperties(arg2).canAlwaysEat())) {
                arg2.startUsingItem(arg3);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(arg2.getItemInHand(arg3));
        }
    }
    */
}
