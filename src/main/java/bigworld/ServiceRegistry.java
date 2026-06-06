package bigworld;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple registry for InstrumentationAPI implementations.
 * Supports multiple registrations; `getInstrumentation` returns the first.
 */
public final class ServiceRegistry {
    private ServiceRegistry() {}

    private static final CopyOnWriteArrayList<InstrumentationAPI> INSTRUMENTATIONS = new CopyOnWriteArrayList<>();

    public static void registerInstrumentation(InstrumentationAPI impl) {
        if (impl == null) return;
        INSTRUMENTATIONS.addIfAbsent(impl);
    }

    public static InstrumentationAPI getInstrumentation() {
        return INSTRUMENTATIONS.isEmpty() ? null : INSTRUMENTATIONS.get(0);
    }

    public static void clear() {
        INSTRUMENTATIONS.clear();
    }
}
