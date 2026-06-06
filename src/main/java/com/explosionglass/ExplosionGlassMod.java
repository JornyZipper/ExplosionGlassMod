package com.explosionglass;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExplosionGlassMod.MODID)
public class ExplosionGlassMod {

    public static final String MODID = "explosionglass";
    public static final String NAME = "EXPLGlass";
    public static final String VERSION = "0.2.0";
    public static final Logger LOGGER = LogUtils.getLogger();
    // Ensure this MODID matches the mod metadata in neoforge.mods.toml and the built JAR

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredHolder<Item, Item> GLASS_SHARD = ITEMS.register("glass_shard", () -> new Item(new Item.Properties().stacksTo(64)));

    public ExplosionGlassMod(IEventBus modEventBus) {
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        // Register event handlers
        NeoForge.EVENT_BUS.register(new ExplosionEventHandler());
        NeoForge.EVENT_BUS.register(new ChainTNTHandler());
        NeoForge.EVENT_BUS.register(new VersionCheckerMod());

        // Register sounds
        SoundRegistry.register(modEventBus);
        ITEMS.register(modEventBus);

        // Initialize sounds and setup custom SoundTypes during common setup when registries are ready
        modEventBus.addListener(this::onCommonSetup);

        // Load config on init
        modEventBus.addListener(this::onModConfig);
    }

    private void onModConfig(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        Config.loadConfig();
    }

    private void onCommonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        // Initialize sound arrays from DeferredHolders now that registries are available
        SoundRegistry.initializeArrays();
        GlassSoundBlocker.setupCustomGlassSounds();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        System.out.println("[ExplosionGlass] Server starting...");
    }
}