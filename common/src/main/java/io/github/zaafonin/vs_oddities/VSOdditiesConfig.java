package io.github.zaafonin.vs_oddities;

import net.minecraftforge.common.ForgeConfigSpec;

public class VSOdditiesConfig {
    public static class Common {
        public static final ForgeConfigSpec SPEC;

        // Mixin-powered features
        // Bug fixes
        public static final ForgeConfigSpec.BooleanValue FIX_BIOME_MISMATCH;
        public static final ForgeConfigSpec.BooleanValue FIX_SHIP_WEATHER;

        // World-to-ship interaction
        public static final ForgeConfigSpec.BooleanValue SHIP_AWARE_CREATE_PROCESSING;
        public static final ForgeConfigSpec.IntValue SHIP_AWARE_CREATE_PROCESSING_BASIN_LAZY_TICK_RATE;
        public static final ForgeConfigSpec.BooleanValue SHIP_AWARE_SCULK;
        public static final ForgeConfigSpec.BooleanValue SHIP_ENTITIES_LEAVE;
        public static final ForgeConfigSpec.DoubleValue SHIP_ENTITIES_LEAVE_FLYDIST;
        public static final ForgeConfigSpec.BooleanValue SHIP_MINECARTS_SNAP;

        // Custom content
        public static final ForgeConfigSpec.BooleanValue ODDITIES_DISPENSER_RECOIL;
        public static final ForgeConfigSpec.BooleanValue ODDITIES_FLOATSTONE_PROPERTIES;
        public static final ForgeConfigSpec.BooleanValue ODDITIES_TREECHOP_SHIPIFY;

        // Utility
        public static final ForgeConfigSpec.BooleanValue UTILS_VS_BREAK;
        public static final ForgeConfigSpec.BooleanValue UTILS_VS_DRY;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            {
                builder.push("Fixes and polish");

                FIX_BIOME_MISMATCH = builder
                        .comment("Newly created ships retain the original biome at this location.")
                        .define("FixBiomeMismatch", true);
                FIX_SHIP_WEATHER = builder
                        .comment("Ships experience world weather, even if the ship biome does not match.")
                        .define("FixShipWeather", true);

                builder.pop();
            }
            {
                builder.push("Interaction and integration");

                SHIP_AWARE_CREATE_PROCESSING = builder
                        .comment("Create processing works between ships, e.g. a mixer on one ship and a basin on another.")
                        .define("ShipAwareCreateProcessing", true);
                SHIP_AWARE_CREATE_PROCESSING_BASIN_LAZY_TICK_RATE = builder
                        .comment("Rate of basins checking for processors above. Smaller value: more often but impacts performance. Default: 3")
                        .defineInRange("ShipAwareCreateProcessing_BasinLazyTickRate", 3, 1, 12);
                SHIP_AWARE_SCULK = builder
                        .comment("Sculk signals propagate between ships and world. Includes compatibility with Echo Chest.")
                        .define("ShipAwareSculk", true);
                SHIP_ENTITIES_LEAVE = builder
                        .comment("\"Shipyard entities\" are untied from the ship on leaving its boundaries.")
                        .define("ShipEntitiesLeave", true);
                SHIP_ENTITIES_LEAVE_FLYDIST = builder
                        .comment("Safe distance before entities leave a ship. Default: 0.05")
                        .defineInRange("ShipEntitiesLeaveFlyDist", 0.05, 0, 32768);
                SHIP_MINECARTS_SNAP = builder
                        .comment("Experimental: Minecarts can snap to rails positioned on ships.")
                        .define("ShipMinecartsSnap", true);

                builder.pop();
            }
            {
                builder.push("Features and oddities");

                ODDITIES_DISPENSER_RECOIL = builder
                        .comment("Dispensers create recoil when launching some projectiles.")
                        .define("OdditiesDispenserRecoil", true);
                ODDITIES_FLOATSTONE_PROPERTIES = builder
                        .comment("Some blocks are capable of negating gravity up to a certain weight.")
                        .define("OdditiesFloatstoneProperties", true);
                ODDITIES_TREECHOP_SHIPIFY = builder
                        .comment("Felling a tree with HT's TreeChop shipfies it.")
                        .define("OdditiesTreeChopShipify", true);

                builder.pop();
            }
            {
                builder.push("Technical utilities");

                UTILS_VS_BREAK = builder
                        .comment("/vs break <ship>, a command to break all blocks on a ship as if they were harvested.")
                        .define("UtilsVSBreak", true);
                UTILS_VS_DRY = builder
                        .comment("/vs dry <ship>, removes all water in a ship, including water in waterlogged blocks.")
                        .define("UtilsVSDry", true);

                builder.pop();
            }
            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ForgeConfigSpec SPEC;

        // Utility
        public static final ForgeConfigSpec.BooleanValue UTILS_DEBUG_OVERLAY;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            {
                builder.push("Fixes and polish");

                UTILS_DEBUG_OVERLAY = builder
                        .comment("Add VS2-specific tooltips to the debug overlay (F3 screen)")
                        .define("UtilsDebugOverlay", true);

                builder.pop();
            }
            SPEC = builder.build();
        }
    }
}
