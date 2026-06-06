package bigworld;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Provider interface for creating physical shards in the world.
 * Implementations handle the spawning and management of projectile shards
 * from block breaks, explosions, and other events.
 * 
 * This is a manual API — client mods must explicitly call these methods.
 * No automatic event subscriptions or hooks are provided by BigWorld.
 */
public interface IShardProvider {
    
    /**
     * Create shards from a block break caused by an explosion.
     * 
     * @param world        The world instance
     * @param blockPos     The position of the broken block
     * @param explosivePos The position of the explosive source (for direction calculation)
     * @param count        Number of shards to spawn (if -1, uses config default)
     * @param impulse      Velocity multiplier for shard motion (1.0 = normal)
     */
    void createShardsOnBlockBreak(World world, BlockPos blockPos, BlockPos explosivePos, int count, float impulse);
    
    /**
     * Create shards from an explosion, detecting all glass blocks in radius.
     * 
     * @param world   The world instance
     * @param center  Center position of the explosion
     * @param radius  Search radius for glass blocks
     * @param impulse Velocity multiplier for shard motion (1.0 = normal)
     */
    void createShardsFromExplosion(World world, Vec3d center, float radius, float impulse);
    
    /**
     * Get the configuration for shard behavior.
     */
    IShardConfig getConfig();
    
}
