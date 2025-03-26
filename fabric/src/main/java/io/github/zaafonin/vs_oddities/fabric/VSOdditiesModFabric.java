package io.github.zaafonin.vs_oddities.fabric;

import net.fabricmc.api.ModInitializer;

import io.github.zaafonin.vs_oddities.VSOdditiesMod;

public final class VSOdditiesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        VSOdditiesMod.init();
    }
}
