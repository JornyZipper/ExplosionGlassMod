package bigworld;

import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static bigworld.CoreConfig.setRenderOptimizationsEnabled;

/**
 * Core entrypoint for BigWorld.
 * Adds logging around initialization, plus simple runtime config helpers.
 */
public final class BWRCore {
    private BWRCore() {}

    private static final Logger LOGGER = LogManager.getLogger("BigWorldCore");
    private static volatile IShardProvider shardProvider = null;

    public static void init() {
        LOGGER.info("Initializing BigWorld v{}", BWR.VERSION);

        // load configuration first
        CoreConfig.load();
        
        // Initialize shard system
        shardProvider = new bigworld.shard.ShardSystem();
        LOGGER.info("Initialized shard system: {}", shardProvider.getConfig());
        
        // discover InstrumentationAPI implementations via ServiceLoader
        ServiceLoader<InstrumentationAPI> loader = ServiceLoader.load(InstrumentationAPI.class);
        int found = 0;
        for (InstrumentationAPI impl : loader) {
            ServiceRegistry.registerInstrumentation(impl);
            LOGGER.debug("Discovered InstrumentationAPI impl: {}", impl.getClass().getName());
            found++;
        }
        LOGGER.info("Registered {} instrumentation implementation(s).", found);
    }

    /**
     * Programmatically register an InstrumentationAPI implementation.
     */
    public static void registerInstrumentation(InstrumentationAPI impl) {
        ServiceRegistry.registerInstrumentation(impl);
        LOGGER.info("Instrumentation registered: {}", (impl == null ? "null" : impl.getClass().getName()));
    }

    /**
     * Get the currently registered instrumentation (first registered) or null.
     */
    public static InstrumentationAPI getInstrumentation() {
        return ServiceRegistry.getInstrumentation();
    }

    /**
     * Reload configuration from disk and apply relevant runtime changes.
     */
    public static void reloadConfig() {
        CoreConfig.load();
        boolean enabled = CoreConfig.isRenderOptimizationsEnabled();
        setRenderOptimizationsEnabled(enabled);
        LOGGER.info("Reloaded config. LOS enabled={}", enabled);
    }

    /**
     * Get the shard provider for manual shard creation.
     * Returns the default provider initialized in init(), or a custom one if registered.
     */
    public static IShardProvider getShardProvider() {
        return shardProvider;
    }

    /**
     * Register a custom shard provider. Only one provider is kept.
     */
    public static void setShardProvider(IShardProvider provider) {
        if (provider != null) {
            shardProvider = provider;
            LOGGER.info("Set custom shard provider: {}", provider.getClass().getName());
        }
    }
    
}

