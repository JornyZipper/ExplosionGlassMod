package com.coders.explosion;

import net.minecraft.client.resources.I18n;
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
    public static final String VERSION = "1.8";

    public static Configuration config;

    public static boolean Mod;
    public static int glassBreakRadius;
    public static String[] glassBlacklist;
    public static boolean useLineOfSight;
    public static boolean glassDrops;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        syncConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new VersionCheckerMod());
    }

    private void syncConfig() {
        // Используем I18n.format для локализации описаний конфигурации
        Mod = config.getBoolean(
                "enabled",
                "general",
                true,
                I18n.format("config.explosionglass.enabled")
        );

        glassBreakRadius = config.getInt(
                "glassBreakRadius",
                "general",
                20,
                1,
                100,
                I18n.format("config.explosionglass.glassBreakRadius")
        );

        glassBlacklist = config.getStringList(
                "glassBlacklist",
                "general",
                new String[0],
                I18n.format("config.explosionglass.glassBlacklist")
        );

        useLineOfSight = config.getBoolean(
                "useLineOfSight",
                "general",
                true,
                I18n.format("config.explosionglass.useLineOfSight")
        );

        glassDrops = config.getBoolean(
                "glassDrops",
                "general",
                false,
                I18n.format("config.explosionglass.glassDrops")
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            syncConfig();
        }
    }

    static {
        Mod = true;
        glassBreakRadius = 20;
        glassBlacklist = new String[0];
        useLineOfSight = true;
        glassDrops = false;
    }
}
