package bigworld;

/**
 * Configuration interface for shard behavior.
 * Defines the contract for how shards behave (damage, lifetime, etc.).
 */
public interface IShardConfig {
    
    /**
     * Get the damage dealt to entities per shard collision.
     */
    float getShardDamage();
    
    /**
     * Get the default number of shards to spawn per block.
     */
    int getDefaultShardCount();
    
    /**
     * Get the lifetime (in ticks) before a shard becomes an item and despawns.
     */
    int getShardLifetime();
    
    /**
     * Get the impulse/velocity multiplier for shards (1.0 = normal).
     */
    float getShardImpulse();
    
    /**
     * Whether shards should damage entities on contact.
     */
    boolean isDamageEnabled();
    
    /**
     * Whether shards should drop as items when they settle.
     */
    boolean isItemDropEnabled();
    
}
