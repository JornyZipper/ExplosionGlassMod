package bigworld.shard;

import bigworld.IShardConfig;
import bigworld.IShardProvider;
import bigworld.ShardConfig;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.block.Block;

import java.util.Random;

/**
 * Default implementation of IShardProvider.
 * Manages shard creation for block breaks and explosions.
 * 
 * Detects glass blocks: GLASS, GLASS_PANE, STAINED_GLASS, STAINED_GLASS_PANE
 * Calculates direction from explosion center and spawns projectile shards.
 */
public class ShardSystem implements IShardProvider {
    
    private final IShardConfig config;
    private final Random random = new Random();
    
    /**
     * Create with default configuration.
     */
    public ShardSystem() {
        this(new ShardConfig());
    }
    
    /**
     * Create with custom configuration.
     */
    public ShardSystem(IShardConfig config) {
        this.config = (config != null) ? config : new ShardConfig();
        ShardEntity.setConfig(this.config);
    }
    
    @Override
    public void createShardsOnBlockBreak(World world, BlockPos blockPos, BlockPos explosivePos, int count, float impulse) {
        if (world.isRemote) {
            return; // server-side only
        }
        
        // check if block is glass
        if (!isGlassBlock(world, blockPos)) {
            return;
        }
        
        // determine spawn count
        int shardCount = (count > 0) ? count : this.config.getDefaultShardCount();
        
        // calculate direction from explosive to block
        Vec3d direction = new Vec3d(
            blockPos.getX() - explosivePos.getX(),
            blockPos.getY() - explosivePos.getY(),
            blockPos.getZ() - explosivePos.getZ()
        ).normalize();
        
        // spawn shards
        spawnShards(world, blockPos, direction, shardCount, impulse);
    }
    
    @Override
    public void createShardsFromExplosion(World world, Vec3d center, float radius, float impulse) {
        if (world.isRemote) {
            return; // server-side only
        }
        
        // find all glass blocks in radius
        int cx = (int) center.x;
        int cy = (int) center.y;
        int cz = (int) center.z;
        
        int searchRadius = (int) Math.ceil(radius) + 1;
        
        for (int x = cx - searchRadius; x <= cx + searchRadius; x++) {
            for (int y = cy - searchRadius; y <= cy + searchRadius; y++) {
                for (int z = cz - searchRadius; z <= cz + searchRadius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    double distance = pos.getDistance(cx, cy, cz);
                    
                    if (distance > radius) {
                        continue;
                    }
                    
                    if (isGlassBlock(world, pos)) {
                        // calculate direction from center to block
                        Vec3d direction = new Vec3d(
                            pos.getX() - center.x,
                            pos.getY() - center.y,
                            pos.getZ() - center.z
                        ).normalize();
                        
                        // spawn shards for this block
                        int shardCount = this.config.getDefaultShardCount();
                        spawnShards(world, pos, direction, shardCount, impulse);
                    }
                }
            }
        }
    }
    
    /**
     * Spawn N shards from a block in a given direction.
     */
    private void spawnShards(World world, BlockPos blockPos, Vec3d direction, int count, float impulse) {
        float baseImpulse = this.config.getShardImpulse() * impulse;
        int lifetime = this.config.getShardLifetime();
        
        for (int i = 0; i < count; i++) {
            // randomize direction (±30 degrees)
            Vec3d randomDirection = randomizeDirection(direction, 0.3);
            
            // add velocity variance
            double velocityMagnitude = 0.5 + random.nextDouble() * 0.5; // 0.5-1.0
            Vec3d velocity = randomDirection.scale(velocityMagnitude * baseImpulse);
            
            // create shard entity
            ShardEntity shard = new ShardEntity(world, lifetime);
            
            // spawn at block center
            shard.setPosition(
                blockPos.getX() + 0.5 + random.nextGaussian() * 0.1,
                blockPos.getY() + 0.5 + random.nextGaussian() * 0.1,
                blockPos.getZ() + 0.5 + random.nextGaussian() * 0.1
            );
            
            // set motion
            shard.motionX = velocity.x;
            shard.motionY = velocity.y;
            shard.motionZ = velocity.z;
            
            world.spawnEntity(shard);
        }
    }
    
    /**
     * Randomize direction vector by ±angle radians.
     */
    private Vec3d randomizeDirection(Vec3d direction, double angleVariance) {
        // rotate around random axis by random angle
        double angle = (random.nextDouble() - 0.5) * angleVariance * 2.0;
        
        // simple spherical randomization
        double theta = random.nextDouble() * Math.PI * 2.0;
        double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
        
        double x = Math.sin(phi) * Math.cos(theta) * Math.cos(angle) + direction.x * Math.sin(angle);
        double y = Math.sin(phi) * Math.sin(theta) * Math.cos(angle) + direction.y * Math.sin(angle);
        double z = Math.cos(phi) * Math.cos(angle) + direction.z * Math.sin(angle);
        
        return new Vec3d(x, y, z).normalize();
    }
    
    /**
     * Check if block is a glass-type block.
     */
    private boolean isGlassBlock(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        
        return block == Blocks.GLASS ||
               block == Blocks.GLASS_PANE ||
               block == Blocks.STAINED_GLASS ||
               block == Blocks.STAINED_GLASS_PANE;
    }
    
    @Override
    public IShardConfig getConfig() {
        return this.config;
    }
    
}
