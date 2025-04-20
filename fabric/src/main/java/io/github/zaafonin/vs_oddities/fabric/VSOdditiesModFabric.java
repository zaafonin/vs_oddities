package io.github.zaafonin.vs_oddities.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import net.fabricmc.api.ModInitializer;

import io.github.zaafonin.vs_oddities.VSOdditiesMod;
import net.minecraftforge.fml.config.ModConfig;

public final class VSOdditiesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        VSOdditiesMod.init();

        ForgeConfigRegistry.INSTANCE.register(VSOdditiesMod.MOD_ID, ModConfig.Type.COMMON, VSOdditiesConfig.Common.SPEC);
        ForgeConfigRegistry.INSTANCE.register(VSOdditiesMod.MOD_ID, ModConfig.Type.CLIENT, VSOdditiesConfig.Client.SPEC);
    }
}
