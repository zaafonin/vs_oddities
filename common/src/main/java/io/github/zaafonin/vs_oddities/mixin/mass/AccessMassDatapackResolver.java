package io.github.zaafonin.vs_oddities.mixin.mass;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.valkyrienskies.mod.common.config.MassDatapackResolver;

import java.util.HashMap;

@Mixin(MassDatapackResolver.class)
public interface AccessMassDatapackResolver {
    @Accessor
    HashMap<ResourceLocation, ?> getMap();
}