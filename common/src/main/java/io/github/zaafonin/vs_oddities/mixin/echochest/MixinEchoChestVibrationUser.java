package io.github.zaafonin.vs_oddities.mixin.echochest;

import io.github.zaafonin.vs_oddities.util.ShipBlockPositionSource;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = { "fuzs.echochest.world.level.block.entity.EchoChestVibrationUser" }, remap = false)
public class MixinEchoChestVibrationUser {
    @ModifyVariable(
            method = "<init>(Lfuzs/echochest/world/level/block/entity/EchoChestBlockEntity;Lnet/minecraft/world/level/gameevent/PositionSource;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static PositionSource replacePositionSource(PositionSource value) {
        return new ShipBlockPositionSource(((AccessBlockPositionSource)value).getPos());
    }
}
