package bigworld;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod(modid = BWR.MODID, name = BWR.NAME, version = BWR.VERSION)
public class BWR
{
    public static final String MODID = "bigworld";
    public static final String NAME = "BigWorld";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @SidedProxy(clientSide = "bigworld.proxy.ClientProxy", serverSide = "bigworld.proxy.CommonProxy")
    public static bigworld.proxy.CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        // initialize core library
        BWRCore.init();
        
        // register shard entity
        EntityRegistry.registerModEntity(
            new net.minecraft.util.ResourceLocation(MODID, "shard_entity"),
            bigworld.shard.ShardEntity.class,
            "ShardEntity",
            0,
            this,
            64,
            1,
            true
        );
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        // register client keybinds (no-op on dedicated server)
        if (proxy != null) proxy.registerKeybinds();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        // No server commands for library-only core
    }
}
