package bigworld.los;

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;

/**
 * Simple primitive cache for LOS results keyed by a packed long.
 */
public final class LOSCache {
    private final Long2BooleanOpenHashMap map = new Long2BooleanOpenHashMap(1024);

    public synchronized void put(long key, boolean hasLOS) {
        map.put(key, hasLOS);
    }

    public synchronized boolean containsKey(long key) {
        return map.containsKey(key);
    }

    public synchronized boolean get(long key) {
        return map.get(key);
    }

    public synchronized void clear() {
        map.clear();
    }
}
