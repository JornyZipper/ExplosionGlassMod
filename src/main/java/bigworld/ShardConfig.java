package bigworld;

/**
 * Default immutable configuration for shards.
 */
public class ShardConfig implements IShardConfig {
    
    private final float shardDamage;
    private final int defaultShardCount;
    private final int shardLifetime;
    private final float shardImpulse;
    private final boolean damageEnabled;
    private final boolean itemDropEnabled;
    
    /**
     * Create config with custom values.
     */
    public ShardConfig(float shardDamage, int defaultShardCount, int shardLifetime, 
                       float shardImpulse, boolean damageEnabled, boolean itemDropEnabled) {
        this.shardDamage = shardDamage;
        this.defaultShardCount = defaultShardCount;
        this.shardLifetime = shardLifetime;
        this.shardImpulse = shardImpulse;
        this.damageEnabled = damageEnabled;
        this.itemDropEnabled = itemDropEnabled;
    }
    
    /**
     * Create config with default values.
     */
    public ShardConfig() {
        this(1.0f, 12, 200, 1.0f, true, true);
    }
    
    @Override
    public float getShardDamage() { return shardDamage; }
    
    @Override
    public int getDefaultShardCount() { return defaultShardCount; }
    
    @Override
    public int getShardLifetime() { return shardLifetime; }
    
    @Override
    public float getShardImpulse() { return shardImpulse; }
    
    @Override
    public boolean isDamageEnabled() { return damageEnabled; }
    
    @Override
    public boolean isItemDropEnabled() { return itemDropEnabled; }
    
    @Override
    public String toString() {
        return String.format("ShardConfig(damage=%.1f, count=%d, lifetime=%d, impulse=%.1f, damageEnabled=%s, itemDropEnabled=%s)",
                shardDamage, defaultShardCount, shardLifetime, shardImpulse, damageEnabled, itemDropEnabled);
    }
    
}
