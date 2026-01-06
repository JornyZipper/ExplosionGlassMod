package com.coders.explosion;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
        modid = ExplosionGlassMod.MODID,
        name = ExplosionGlassMod.NAME,
        version = ExplosionGlassMod.VERSION,
        dependencies = "required-after:bwr_core",
        acceptableRemoteVersions = "*",
        guiFactory = "com.coders.explosion.ConfigGuiFactory"
)
public class ExplosionGlassMod {
    public static final String MODID = "explglass";
    public static final String NAME = "EXPLGlass";
        public static final String VERSION = "2.0";

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
                // If the external core mod `bwr_core` is present, try to register our instrumentation.
                try {
                        if (net.minecraftforge.fml.common.Loader.isModLoaded("bwr_core")) {
                                // Try several possible BRCore class names reflectively
                                String[] candidates = new String[]{
                                        "bwr.core.BRCore",
                                        "brcore.BRCore",
                                        "BRCore",
                                        "com.bwr.core.BRCore",
                                        "com.brcore.BRCore"
                                };
                                Class<?> brcoreClass = null;
                                for (String name : candidates) {
                                        try {
                                                brcoreClass = Class.forName(name);
                                                break;
                                        } catch (ClassNotFoundException ignored) {}
                                }

                                if (brcoreClass != null) {
                                        try {
                                                // Try static registerInstrumentation(Object)
                                                java.lang.reflect.Method m = brcoreClass.getMethod("registerInstrumentation", Object.class);
                                                m.invoke(null, new com.coders.explosion.instrumentation.ExplosionGlassInstrumentation());
                                        } catch (NoSuchMethodException e1) {
                                                try {
                                                        // Try static registerInstrumentation(Class)
                                                        java.lang.reflect.Method m2 = brcoreClass.getMethod("registerInstrumentation", Class.class);
                                                        m2.invoke(null, com.coders.explosion.instrumentation.ExplosionGlassInstrumentation.class);
                                                } catch (NoSuchMethodException e2) {
                                                        try {
                                                                // Try instance registration via INSTANCE field
                                                                java.lang.reflect.Field f = brcoreClass.getField("INSTANCE");
                                                                Object instance = f.get(null);
                                                                java.lang.reflect.Method m3 = brcoreClass.getMethod("registerInstrumentation", Object.class);
                                                                m3.invoke(instance, new com.coders.explosion.instrumentation.ExplosionGlassInstrumentation());
                                                        } catch (Exception ignored) {
                                                                System.out.println("ExplosionGlass: could not find BRCore.registerInstrumentation signature.");
                                                        }
                                                } catch (Exception ex) {
                                                        System.out.println("ExplosionGlass: error invoking BRCore.registerInstrumentation(Class)");
                                                }
                                        } catch (Exception ex) {
                                                System.out.println("ExplosionGlass: error invoking BRCore.registerInstrumentation(Object)");
                                        }
                                } else {
                                        System.out.println("ExplosionGlass: BRCore class not found despite bwr_core present.");
                                }
                        }
                } catch (Throwable t) {
                        System.out.println("ExplosionGlass: error while attempting BRCore integration: " + t.getMessage());
                }
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
