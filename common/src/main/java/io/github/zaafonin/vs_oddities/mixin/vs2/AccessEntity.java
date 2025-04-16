package io.github.zaafonin.vs_oddities.mixin.vs2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface AccessEntity {
    @Accessor
    Vec3 getPosition();
    @Accessor
    void setPosition(Vec3 position);
}