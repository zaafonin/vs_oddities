package io.github.zaafonin.vs_oddities.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import io.github.zaafonin.vs_oddities.VSOdditiesMod;

@Mod(VSOdditiesMod.MOD_ID)
public final class VSOdditiesModForge {
    public VSOdditiesModForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(VSOdditiesMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        VSOdditiesMod.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VSOdditiesConfig.Common.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VSOdditiesConfig.Client.SPEC);
    }
}
