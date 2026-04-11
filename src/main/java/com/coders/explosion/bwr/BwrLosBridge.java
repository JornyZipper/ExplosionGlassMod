package com.coders.explosion.bwr;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public final class BwrLosBridge {
    private BwrLosBridge() {}

    /**
     * Ask BWR-Core ILOS provider whether 'from' can see center of targetPos.
     * Returns false if provider is not available or any error occurs.
     */
    public static boolean canSee(World world, Vec3d from, BlockPos targetPos) {
        try {
            if (!Loader.isModLoaded("bwr_core")) return false;
        } catch (Throwable t) {
            // In unit tests or non-Forge envs Loader may not be initialized — treat as absent
            return false;
        }

        if (from == null || targetPos == null) return false;

        try {
            // Try LOSManager first: com.coders.bwr.core.los.LOSManager(boolean useCache)
            try {
                Class<?> losManager = Class.forName("com.coders.bwr.core.los.LOSManager");
                try {
                    Object manager = losManager.getConstructor(boolean.class).newInstance(true);
                    // try common method names
                    for (String mName : new String[]{"hasLineOfSight", "canSee", "isVisible"}) {
                        try {
                            java.lang.reflect.Method m = losManager.getMethod(mName, World.class, Vec3d.class, BlockPos.class);
                            Object res = m.invoke(manager, world, from, targetPos);
                            if (res instanceof Boolean) return (Boolean) res;
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                } catch (NoSuchMethodException ignored) {
                }
            } catch (ClassNotFoundException ignored) {
            }

            // Try ILOSProvider singleton: com.coders.bwr.core.los.ILOSProvider
            try {
                Class<?> iface = Class.forName("com.coders.bwr.core.los.ILOSProvider");
                // try static INSTANCE field
                try {
                    java.lang.reflect.Field f = iface.getField("INSTANCE");
                    Object provider = f.get(null);
                    for (String mName : new String[]{"hasLineOfSight", "canSee", "isVisible"}) {
                        try {
                            java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, Vec3d.class, BlockPos.class);
                            Object res = m.invoke(provider, world, from, targetPos);
                            if (res instanceof Boolean) return (Boolean) res;
                        } catch (NoSuchMethodException ignored) {}
                    }
                } catch (NoSuchFieldException ignored) {
                    // try static getter getProvider()
                    try {
                        Class<?> providerHolder = Class.forName("com.coders.bwr.core.BWRCore");
                        java.lang.reflect.Method get = providerHolder.getMethod("getILOSProvider");
                        Object provider = get.invoke(null);
                        for (String mName : new String[]{"hasLineOfSight", "canSee", "isVisible"}) {
                            try {
                                java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, Vec3d.class, BlockPos.class);
                                Object res = m.invoke(provider, world, from, targetPos);
                                if (res instanceof Boolean) return (Boolean) res;
                            } catch (NoSuchMethodException ignored2) {}
                        }
                    } catch (Exception ignored2) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        } catch (Throwable t) {
            System.out.println("BwrLosBridge: error while invoking BWR-Core LOS: " + t.getMessage());
        }

        return false;
    }
}
