package com.coders.explosion.bwr;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public final class BwrGlassBridge {
    private BwrGlassBridge() {}

    /**
     * Try to invoke BigWorld glass shard API to handle glass breaking.
     * NOTE: Currently disabled - BigWorld API not easily discoverable
     * Returns false to use fallback destruction
     */
    public static boolean spawnGlassShards(World world, BlockPos pos, IBlockState state, Vec3d explosionPos) {
        // Disabled for now - fallback destruction is used instead
        return false;
        /*
        try {
            if (!Loader.isModLoaded("bigworld")) {
                System.out.println("[BwrGlassBridge] BigWorld not found");
                return false;
            }
        } catch (Throwable t) {
            System.out.println("[BwrGlassBridge] Error checking BigWorld: " + t.getMessage());
            return false;
        }

        // Custom texture for glass shards
        ResourceLocation glassTexture = new ResourceLocation("explglass", "textures/glass");
        
        try {
            // 1) Try common GlassShards static class WITH TEXTURE
            try {
                Class<?> cls = Class.forName("com.coders.bwr.core.glass.GlassShards");
                System.out.println("[BwrGlassBridge] Found GlassShards class");
                
                // Try WITH texture parameter first
                for (String mName : new String[]{"spawnShards", "spawn", "spawnGlassShards"}) {
                    try {
                        java.lang.reflect.Method m = cls.getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class, ResourceLocation.class);
                        System.out.println("[BwrGlassBridge] Found method with texture: " + mName);
                        m.invoke(null, world, pos, state, explosionPos, glassTexture);
                        System.out.println("[BwrGlassBridge] Successfully invoked GlassShards." + mName + " with texture");
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                }
                
                // Try WITHOUT texture parameter (fallback)
                for (String mName : new String[]{"spawnShards", "spawn", "spawnGlassShards"}) {
                    try {
                        java.lang.reflect.Method m = cls.getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class);
                        System.out.println("[BwrGlassBridge] Found method without texture: " + mName);
                        m.invoke(null, world, pos, state, explosionPos);
                        System.out.println("[BwrGlassBridge] Successfully invoked GlassShards." + mName);
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                }
            } catch (ClassNotFoundException ignored) {
                System.out.println("[BwrGlassBridge] GlassShards class not found");
            }

            // 2) Try BigWorldCore static util class WITH AND WITHOUT TEXTURE
            try {
                Class<?> core = Class.forName("com.coders.bwr.core.BWRCore");
                System.out.println("[BwrGlassBridge] Found BigWorldCore class");
                
                // Try WITH texture first
                for (String mName : new String[]{"spawnGlassShards", "spawnShards"}) {
                    try {
                        java.lang.reflect.Method m = core.getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class, ResourceLocation.class);
                        System.out.println("[BwrGlassBridge] Found BigWorldCore method with texture: " + mName);
                        m.invoke(null, world, pos, state, explosionPos, glassTexture);
                        System.out.println("[BwrGlassBridge] Successfully invoked BigWorldCore." + mName + " with texture");
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                }
                
                // Fallback to without texture
                for (String mName : new String[]{"spawnGlassShards", "spawnShards"}) {
                    try {
                        java.lang.reflect.Method m = core.getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class);
                        System.out.println("[BwrGlassBridge] Found BigWorldCore method without texture: " + mName);
                        m.invoke(null, world, pos, state, explosionPos);
                        System.out.println("[BwrGlassBridge] Successfully invoked BigWorldCore." + mName);
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                }
            } catch (ClassNotFoundException ignored) {
                System.out.println("[BwrGlassBridge] BigWorldCore class not found");
            }

            // 3) Try provider interface singleton: com.coders.bwr.core.glass.IGlassShardProvider
            try {
                Class<?> iface = Class.forName("com.coders.bwr.core.glass.IGlassShardProvider");
                System.out.println("[BwrGlassBridge] Found IGlassShardProvider interface");
                // try INSTANCE field
                try {
                    java.lang.reflect.Field f = iface.getField("INSTANCE");
                    Object provider = f.get(null);
                    System.out.println("[BwrGlassBridge] Found INSTANCE provider");
                    
                    // Try WITH texture first
                    for (String mName : new String[]{"spawnShards", "spawn"}) {
                        try {
                            java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class, ResourceLocation.class);
                            m.invoke(provider, world, pos, state, explosionPos, glassTexture);
                            System.out.println("[BwrGlassBridge] Successfully invoked IGlassShardProvider." + mName + " with texture");
                            return true;
                        } catch (NoSuchMethodException ignored) {}
                    }
                    
                    // Fallback to without texture
                    for (String mName : new String[]{"spawnShards", "spawn"}) {
                        try {
                            java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class);
                            m.invoke(provider, world, pos, state, explosionPos);
                            System.out.println("[BwrGlassBridge] Successfully invoked IGlassShardProvider." + mName);
                            return true;
                        } catch (NoSuchMethodException ignored) {}
                    }
                } catch (NoSuchFieldException ignored) {
                    // try static getter
                    try {
                        java.lang.reflect.Method get = iface.getMethod("getProvider");
                        Object provider = get.invoke(null);
                        System.out.println("[BwrGlassBridge] Found getProvider() provider");
                        
                        // Try WITH texture first
                        for (String mName : new String[]{"spawnShards", "spawn"}) {
                            try {
                                java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class, ResourceLocation.class);
                                m.invoke(provider, world, pos, state, explosionPos, glassTexture);
                                System.out.println("[BwrGlassBridge] Successfully invoked provider." + mName + " with texture");
                                return true;
                            } catch (NoSuchMethodException ignored2) {}
                        }
                        
                        // Fallback to without texture
                        for (String mName : new String[]{"spawnShards", "spawn"}) {
                            try {
                                java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class);
                                m.invoke(provider, world, pos, state, explosionPos);
                                System.out.println("[BwrGlassBridge] Successfully invoked provider." + mName);
                                return true;
                            } catch (NoSuchMethodException ignored2) {}
                        }
                    } catch (NoSuchMethodException ignored2) {}
                }
            } catch (ClassNotFoundException ignored) {
                System.out.println("[BwrGlassBridge] IGlassShardProvider interface not found");
            }

            // 4) Try generic provider on BigWorldCore: getGlassShardProvider()
            try {
                Class<?> core = Class.forName("com.coders.bwr.core.BWRCore");
                System.out.println("[BwrGlassBridge] Trying BigWorldCore.getGlassShardProvider()");
                try {
                    java.lang.reflect.Method get = core.getMethod("getGlassShardProvider");
                    Object provider = get.invoke(null);
                    System.out.println("[BwrGlassBridge] Got provider from getGlassShardProvider()");
                    
                    // Try WITH texture first
                    for (String mName : new String[]{"spawnShards", "spawn"}) {
                        try {
                            java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class, ResourceLocation.class);
                            m.invoke(provider, world, pos, state, explosionPos, glassTexture);
                            System.out.println("[BwrGlassBridge] Successfully invoked provider." + mName + " with texture");
                            return true;
                        } catch (NoSuchMethodException ignored2) {}
                    }
                    
                    // Fallback to without texture
                    for (String mName : new String[]{"spawnShards", "spawn"}) {
                        try {
                            java.lang.reflect.Method m = provider.getClass().getMethod(mName, World.class, BlockPos.class, IBlockState.class, Vec3d.class);
                            m.invoke(provider, world, pos, state, explosionPos);
                            System.out.println("[BwrGlassBridge] Successfully invoked provider." + mName);
                            return true;
                        } catch (NoSuchMethodException ignored2) {}
                    }
                } catch (NoSuchMethodException ignored) {
                    System.out.println("[BwrGlassBridge] getGlassShardProvider() method not found");
                }
            } catch (ClassNotFoundException ignored) {
                System.out.println("[BwrGlassBridge] BigWorldCore not found in second attempt");
            }

        } catch (Throwable t) {
            System.out.println("BwrGlassBridge: error invoking BigWorld glass API: " + t.getMessage());
            t.printStackTrace();
            return false;
        }

        return false;
        */
    }
}
