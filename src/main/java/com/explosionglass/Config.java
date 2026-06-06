package com.explosionglass;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable or disable ExplosionGlass ✅ / ❌")
            .define("enabled", true);

    public static final ModConfigSpec.IntValue GLASS_BREAK_RADIUS = BUILDER
            .comment("Explosion radius without LoS check")
            .defineInRange("glassBreakRadius", 20, 1, 100);

    public static final ModConfigSpec.IntValue GLASS_BREAK_RADIUS_WITH_LOS = BUILDER
            .comment("Explosion radius with LoS check")
            .defineInRange("glassBreakRadiusWithLoS", 10, 1, 100);

    public static final ModConfigSpec.ConfigValue<List<String>> GLASS_BLACKLIST = BUILDER
            .comment("Blocks that should NOT break when exploded 🖋️")
            .define("glassBlacklist", List.of());

    public static final ModConfigSpec.ConfigValue<List<String>> GLASS_WHITELIST = BUILDER
            .comment("Blocks that ALWAYS break regardless of radius or LoS 🖋️")
            .define("glassWhitelist", List.of());

    public static final ModConfigSpec.BooleanValue USE_LINE_OF_SIGHT = BUILDER
            .comment("Use line of sight to determine which glass breaks ✅ / ❌")
            .define("useLineOfSight", true);

    public static final ModConfigSpec.BooleanValue GLASS_DROPS = BUILDER
            .comment("Enable glass drops ✅ / ❌")
            .define("glassDrops", false);

    public static final ModConfigSpec.DoubleValue GLASS_DROP_CHANCE = BUILDER
            .comment("Chance for glass to drop when broken (0.0 - 1.0) 🖋️")
            .defineInRange("glassDropChance", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LOS_IGNORE_DISTANCE = BUILDER
            .comment("Number of blocks to ignore obstacles in LoS")
            .defineInRange("loSIgnoreDistance", 10.0, 0.0, 50.0);

    public static final ModConfigSpec.IntValue HAND_BREAK_SHARD_COUNT = BUILDER
            .comment("Number of shards spawned when manually breaking glass or ice")
            .defineInRange("handBreakShardCount", 3, 0, 20);

    public static final ModConfigSpec.BooleanValue GLASS_SHARD_DAMAGE_ENABLED = BUILDER
            .comment("Enable damage from flying glass shards")
            .define("glassShardDamageEnabled", true);

    public static final ModConfigSpec.BooleanValue USE_3D_WAVE = BUILDER
            .comment("Use spherical 3D wave raycasting for LoS glass breaking ✅ / ❌")
            .define("use3DWave", true);

    public static final ModConfigSpec.DoubleValue GLASS_SHARD_DAMAGE = BUILDER
            .comment("Amount of damage caused by shard collision")
            .defineInRange("glassShardDamage", 1.0, 0.0, 10.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    // Runtime values
    public static boolean enabled;
    public static int glassBreakRadius;
    public static int glassBreakRadiusWithLoS;
    public static List<String> glassBlacklist;
    public static List<String> glassWhitelist;
    public static boolean useLineOfSight;
    public static boolean glassDrops;
    public static double glassDropChance;
        public static boolean use3DWave;
    public static double loSIgnoreDistance;
    public static int handBreakShardCount;
    public static boolean glassShardDamageEnabled;
    public static double glassShardDamage;

    public static void loadConfig() {
        enabled = ENABLED.get();
        glassBreakRadius = GLASS_BREAK_RADIUS.get();
        glassBreakRadiusWithLoS = GLASS_BREAK_RADIUS_WITH_LOS.get();
        glassBlacklist = GLASS_BLACKLIST.get();
        glassWhitelist = GLASS_WHITELIST.get();
        useLineOfSight = USE_LINE_OF_SIGHT.get();
        glassDrops = GLASS_DROPS.get();
        glassDropChance = GLASS_DROP_CHANCE.get();
        use3DWave = USE_3D_WAVE.get();
        loSIgnoreDistance = LOS_IGNORE_DISTANCE.get();
        handBreakShardCount = HAND_BREAK_SHARD_COUNT.get();
        glassShardDamageEnabled = GLASS_SHARD_DAMAGE_ENABLED.get();
        glassShardDamage = GLASS_SHARD_DAMAGE.get();
    }
}
