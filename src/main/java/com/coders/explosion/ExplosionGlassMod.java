package com.coders.explosion;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod(
        modid = ExplosionGlassMod.MODID,
        name = ExplosionGlassMod.NAME,
        version = ExplosionGlassMod.VERSION,
        acceptableRemoteVersions = "*",
        guiFactory = "com.coders.explosion.ConfigGuiFactory"
)
public class ExplosionGlassMod {
    public static final String MODID = "explosionglass";
    public static final String NAME = "ExplosionGlass";
    public static final String VERSION = "1.9.2";

    public static Configuration config;

    public static boolean Mod;
    public static int glassBreakRadius;          // радиус без LoS
    public static int glassBreakRadiusWithLoS;  // радиус с LoS
    public static String[] glassBlacklist;
    public static String[] glassWhitelist;
    public static boolean useLineOfSight;
    public static boolean glassDrops;
    public static double glassDropChance;       // 0.0 - 1.0
    public static double loSIgnoreDistance;     // блоков игнорировать при LoS

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        loadConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        MinecraftForge.EVENT_BUS.register(new VersionCheckerMod());
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void loadConfig() {
        if (config == null) return;
        
        Mod = config.getBoolean(
                "enabled",
                "general",
                true,
                "Enable or disable ExplosionGlass"
        );

        glassBreakRadius = config.getInt(
                "glassBreakRadius",
                "general",
                20,
                1,
                100,
                "Explosion radius without LoS check"
        );

        glassBreakRadiusWithLoS = config.getInt(
                "glassBreakRadiusWithLoS",
                "general",
                10,
                1,
                100,
                "Explosion radius with LoS check"
        );

        glassBlacklist = config.getStringList(
                "glassBlacklist",
                "general",
                new String[0],
                "Blocks that should NOT break when exploded"
        );

        glassWhitelist = config.getStringList(
                "glassWhitelist",
                "general",
                new String[0],
                "Blocks that ALWAYS break regardless of radius or LoS"
        );

        useLineOfSight = config.getBoolean(
                "useLineOfSight",
                "general",
                true,
                "Use line of sight to determine which glass breaks"
        );

        glassDrops = config.getBoolean(
                "glassDrops",
                "general",
                false,
                "Enable glass drops - false by default"
        );

        glassDropChance = config.getFloat(
                "glassDropChance",
                "general",
                1.0f,
                0.0f,
                1.0f,
                "Chance for glass to drop when broken (0.0 - 1.0)"
        );

        loSIgnoreDistance = config.getFloat(
                "loSIgnoreDistance",
                "general",
                10.0f,
                0.0f,
                50.0f,
                "Number of blocks to ignore obstacles in LoS"
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            loadConfig();
        }
    }

    static {
        Mod = true;
        glassBreakRadius = 20;
        glassBreakRadiusWithLoS = 10;
        glassBlacklist = new String[0];
        glassWhitelist = new String[0];
        useLineOfSight = true;
        glassDrops = false;
        glassDropChance = 1.0;
        loSIgnoreDistance = 10.0;
    }
}
