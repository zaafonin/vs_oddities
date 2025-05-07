package io.github.zaafonin.vs_oddities.mixin;

import io.github.zaafonin.vs_oddities.VSOdditiesConfig;
import me.fallenbreath.conditionalmixin.api.mixin.ConditionTester;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.AbstractMap;
import java.util.Map;

public class VSOdditiesForgeConfigConditionTester implements ConditionTester
{
    static Map<String, ForgeConfigSpec.BooleanValue> configMap = Map.ofEntries(
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinChunkAccess", VSOdditiesConfig.Common.FIX_BIOME_MISMATCH),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinServerLevel", VSOdditiesConfig.Common.FIX_SHIP_WEATHER),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinMechanicalMixerBlock", VSOdditiesConfig.Common.SHIP_AWARE_CREATE_PROCESSING),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinBasinBlockEntity", VSOdditiesConfig.Common.SHIP_AWARE_CREATE_PROCESSING),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinBasinOperatingBlockEntity", VSOdditiesConfig.Common.SHIP_AWARE_CREATE_PROCESSING),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinBasinRecipe", VSOdditiesConfig.Common.SHIP_AWARE_CREATE_PROCESSING),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinEuclideanGameEventListenerRegistry", VSOdditiesConfig.Common.SHIP_AWARE_SCULK),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinGameEventDispatcher", VSOdditiesConfig.Common.SHIP_AWARE_SCULK),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinVibrationSystem", VSOdditiesConfig.Common.SHIP_AWARE_SCULK),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinEntity", VSOdditiesConfig.Common.SHIP_ENTITIES_LEAVE),
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinPrimedTnt", VSOdditiesConfig.Common.SHIP_ENTITIES_LEAVE),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinAbstractMinecart", VSOdditiesConfig.Common.SHIP_MINECARTS_SNAP),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinDispenseItemBehavior", VSOdditiesConfig.Common.ODDITIES_DISPENSER_RECOIL),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinFellTreeResult", VSOdditiesConfig.Common.ODDITIES_TREECHOP_SHIPIFY),

            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinVSCommands", VSOdditiesConfig.Common.UTILS_VS_BREAK),
            // TODO: BREAK vs DRY?
            new AbstractMap.SimpleEntry<String, ForgeConfigSpec.BooleanValue>("MixinDebugScreenOverlay", VSOdditiesConfig.Client.UTILS_DEBUG_OVERLAY)
    );

    @Override
    public boolean isSatisfied(String mixinClassName)
    {
        return configMap.get(mixinClassName).get();
    }
}