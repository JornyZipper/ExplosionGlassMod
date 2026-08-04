package com.coders.explosion;

import bigworld.InstrumentationAPI;
import com.coders.explosion.instrumentation.ExplosionGlassInstrumentation;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraftforge.fml.common.Loader;

@Mod(
        modid = ExplosionGlassMod.MODID,
        name = ExplosionGlassMod.NAME,
        version = "2.2.2.1",
        guiFactory = "com.coders.explosion.ConfigGuiFactory"
)
public class ExplosionGlassMod {

        @SidedProxy(
                clientSide = "com.coders.explosion.ClientProxy",
                serverSide = "com.coders.explosion.CommonProxy"
        )
        public static CommonProxy proxy;
  
    public static final String MODID = "explglass";
    public static final String NAME = "EXPLGlass";
        public static final String VERSION = "2.2.2.1";

    public static Configuration config;

    public static boolean Mod;
    public static int glassBreakRadius;          // радиус без LoS
    public static int glassBreakRadiusWithLoS;  // радиус с LoS
    public static Set<String> BLACKLIST = new HashSet<>();
    public static Set<String> WHITELIST = new HashSet<>();
    public static boolean useLineOfSight;
    public static boolean glassDrops;
    public static boolean showUpdateNotice;
    public static boolean BWR_PRESENT;
    public static double glassDropChance;       // 0.0 - 1.0
    public static double loSIgnoreDistance;
     // блоков игнорировать при LoS

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GlassSoundBlocker.setupCustomGlassSounds();
        System.out.println("[ExplosionGlass] PRE-INIT: Custom glass & ice sounds applied!");
        config = new Configuration(new File(event.getModConfigurationDirectory(), "explosionglass.cfg"));
        loadConfig();
        BWR_PRESENT = detectBwrCore();
        MinecraftForge.EVENT_BUS.register(this);
                // Register internal BigWorld instrumentation built into the mod.
        try {
            bigworld.BWRCore.registerInstrumentation((InstrumentationAPI) new ExplosionGlassInstrumentation());
            System.out.println("[ExplosionGlass] PRE-INIT: Embedded BigWorld instrumentation registered.");
        } catch (Throwable t) {
            System.out.println("[ExplosionGlass] PRE-INIT: Could not register embedded BigWorld instrumentation: " + t.getMessage());
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
                System.out.println("[ExplosionGlass] INIT: Registering event handlers...");
                MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
                MinecraftForge.EVENT_BUS.register(new ChainTNTHandler());
                MinecraftForge.EVENT_BUS.register(new VersionCheckerMod());
                MinecraftForge.EVENT_BUS.register(this);
                System.out.println("[ExplosionGlass] INIT: Event handlers registered!");
                System.out.println("[ExplosionGlass] INIT: Mod enabled = " + Mod);
                System.out.println("[ExplosionGlass] INIT: Glass break radius = " + glassBreakRadius);

                proxy.registerRenderers();

                // Register client proxy for sound events
                if (proxy instanceof ClientProxy) {
                    MinecraftForge.EVENT_BUS.register(proxy);
                }

                // Setup custom sounds for glass and ice
                GlassSoundBlocker.setupCustomGlassSounds();
    }

    public static boolean isBigWorldDetected() {
        return BWR_PRESENT;
    }

    private static boolean detectBwrCore() {
        boolean present = false;
        if (Loader.isModLoaded("bwr_core") || Loader.isModLoaded("bwr-core") || Loader.isModLoaded("bwr core")) {
            System.out.println("[ExplosionGlass] BWR core detected via Forge Loader: bwr_core / bwr-core");
            present = true;
        } else {
            File modsDir = new File(System.getProperty("user.dir"), "mods");
            if (!modsDir.exists() || !modsDir.isDirectory()) {
                System.out.println("[ExplosionGlass] BWR core not detected: mods folder not found");
                present = false;
            } else {
                File[] entries = modsDir.listFiles();
                if (entries == null) {
                    System.out.println("[ExplosionGlass] BWR core not detected: unable to list mods folder");
                    present = false;
                } else {
                    for (File entry : entries) {
                        if (entry.isDirectory()) {
                            if (isBwrCoreFolder(entry)) {
                                System.out.println("[ExplosionGlass] BWR core detected in mods folder: " + entry.getAbsolutePath());
                                present = true;
                                break;
                            }
                            continue;
                        }

                        if (!entry.isFile()) {
                            continue;
                        }

                        String lowerName = entry.getName().toLowerCase(Locale.ROOT);
                        if (!lowerName.endsWith(".jar") && !lowerName.endsWith(".zip")) {
                            continue;
                        }

                        if (isBwrCoreArchive(entry)) {
                            System.out.println("[ExplosionGlass] BWR core detected in mods archive: " + entry.getAbsolutePath());
                            present = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!present) {
            System.out.println("[ExplosionGlass] BWR core not detected in mods folder");
        }

        return present;
    }

    private static boolean isBwrCoreFolder(File folder) {
        String folderName = folder.getName().toLowerCase(Locale.ROOT);
        return folderName.contains("bwr-core") || folderName.contains("bwr_core") || folderName.contains("bwr core");
    }

    private static boolean isBwrCoreArchive(File archiveFile) {
        if (isCurrentModArchive(archiveFile)) {
            return false;
        }

        String fileName = archiveFile.getName().toLowerCase(Locale.ROOT);
        if (fileName.contains("bwr-core") || fileName.contains("bwr_core") || fileName.contains("bwr core")) {
            return true;
        }

        try (ZipFile zipFile = new ZipFile(archiveFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName().toLowerCase(Locale.ROOT);
                if (entryName.contains("bwr_core/") || entryName.contains("bwr-core/") || entryName.contains("bwr core/")) {
                    return true;
                }

                if (!entryName.equals("mcmod.info") && !entryName.equals("meta-inf/manifest.mf")) {
                    continue;
                }

                String content = readZipEntry(zipFile, entry);
                String lowerContent = content.toLowerCase(Locale.ROOT);
                if ((lowerContent.contains("modid") || lowerContent.contains("name"))
                        && (lowerContent.contains("bwr_core") || lowerContent.contains("bwr-core") || lowerContent.contains("bwr core"))) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            // Ignore unreadable archives and continue scanning.
        }

        return false;
    }

    private static boolean isCurrentModArchive(File archiveFile) {
        try {
            URL codeSource = ExplosionGlassMod.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeSource == null) {
                return false;
            }

            String externalForm = codeSource.toExternalForm();
            if (externalForm.startsWith("jar:")) {
                int separator = externalForm.indexOf("!/");
                if (separator >= 0) {
                    externalForm = externalForm.substring(4, separator);
                }
            }

            if (!externalForm.startsWith("file:")) {
                return false;
            }

            File currentModJar = new File(new URI(externalForm)).getCanonicalFile();
            File candidate = archiveFile.getCanonicalFile();
            return currentModJar.equals(candidate);
        } catch (IOException | URISyntaxException ignored) {
            return false;
        }
    }

    private static String readZipEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (InputStream inputStream = zipFile.getInputStream(entry);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toString("UTF-8");
        }
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

        BLACKLIST = new HashSet<>(Arrays.asList(
                config.getStringList(
                        "glassBlackList",
                        "general",
                        new String[0],
                        "Block that should NOT break when exploded"
                )
        ));

        WHITELIST = new HashSet<>(Arrays.asList(
                config.getStringList(
                        "glassWhitelist",
                        "general",
                        new String[0],
                        "Blocks that ALWAYS break regardless of radius or LoS"
                )
        ));

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

        showUpdateNotice = config.getBoolean(
                "showUpdateNotice",
                "general",
                true,
                "Show the last update notice window when the game starts"
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

        BLACKLIST = new HashSet<>();
        WHITELIST = new HashSet<>();

        useLineOfSight = true;
        glassDrops = false;
        showUpdateNotice = true;
        glassDropChance = 1.0;
        loSIgnoreDistance = 10.0;
    }
}
