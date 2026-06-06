package bigworld;

/**
 * Public instrumentation API for mods (e.g. explosionGlass) to hook into core features.
 */
public interface InstrumentationAPI {
    /**
     * Instrument a target object from explosionGlass to apply core optimizations.
     * Implementation details are intentionally generic to avoid tight coupling.
     */
    void instrumentExplosionGlass(Object target);
}
