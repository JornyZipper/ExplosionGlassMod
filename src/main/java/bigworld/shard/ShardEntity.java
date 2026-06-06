package bigworld.shard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Entity representing a single physical shard in the world.
 * 
 * Lifecycle: flying → landing (on ground/block) → becomes item entity → despawned
 * 
 * Physics:
 * - Gravity: 0.03 per tick (downward)
 * - Damping: motion *= 0.98 per tick (natural friction)
 * - Ground collision: reduces motion, sets hasHitGround flag
 * - Entity collision: deals damage (if enabled), self-destructs
 * - Lifetime: after N ticks, converts to ItemStack and despawns
 * 
 * Server-side only (checks world.isRemote internally).
 */
public class ShardEntity extends Entity {
    
    private static final DataParameter<Integer> LIFETIME = EntityDataManager.createKey(ShardEntity.class, DataSerializers.VARINT);
    
    private int age = 0;
    private int lifetime = 200; // default, will be synced
    private boolean hasHitGround = false;
    private boolean damageDealt = false;
    
    // Config reference (for damage, item drop, etc.)
    private static bigworld.IShardConfig config = null;
    
    /**
     * Set the global shard config (called once during init).
     */
    public static void setConfig(bigworld.IShardConfig cfg) {
        config = cfg;
    }
    
    /**
     * Constructor for entity creation.
     */
    public ShardEntity(World world) {
        super(world);
        this.setSize(0.2f, 0.2f);
    }
    
    /**
     * Constructor with lifetime override.
     */
    public ShardEntity(World world, int lifetime) {
        this(world);
        this.lifetime = lifetime;
    }
    
    @Override
    protected void entityInit() {
        this.dataManager.register(LIFETIME, this.lifetime);
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        
        // skip on client side (rendering only)
        if (this.world.isRemote) {
            return;
        }
        
        // apply gravity
        if (!hasHitGround) {
            this.motionY -= 0.03D;
        }
        
        // move entity
        this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        
        // apply friction damping
        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;
        
        // detect ground collision
        if (this.onGround && !hasHitGround) {
            hasHitGround = true;
            this.motionX *= 0.5D;
            this.motionY *= 0.1D;
            this.motionZ *= 0.5D;
        }
        
        age++;
        
        // expire after lifetime ticks
        if (age >= lifetime) {
            this.dropAsItem();
            this.setDead();
            return;
        }
        
        // check entity collisions (after 2 ticks to avoid immediate self-collision)
        if (!this.world.isRemote && age > 2) {
            checkEntityCollisions();
        }
        
        // final phase: convert to item+drop after near-death
        if (hasHitGround && age > lifetime - 20) {
            this.dropAsItem();
            this.setDead();
        }
    }
    
    /**
     * Check for nearby entities and deal damage on contact.
     * Now includes LOS check to ensure shard can "see" the entity.
     */
    private void checkEntityCollisions() {
        if (config == null || !config.isDamageEnabled()) {
            return;
        }
        
        AxisAlignedBB box = this.getEntityBoundingBox().expand(0.5, 0.5, 0.5);
        List<Entity> nearby = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        
        for (Entity entity : nearby) {
            // skip other shards, items, armor stands
            if (entity instanceof ShardEntity || entity instanceof EntityItem) {
                continue;
            }
            if (entity.getName().equals("ArmorStand")) {
                continue;
            }
            
            // Вычислить LOS между текущей позицией осколка и центром сущности
            boolean hasLOS = ShardAPI.computeShardTrajectoryAndCheckLOS(
                this.world,
                this.posX, this.posY + this.getEyeHeight(), this.posZ,
                entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ,
                true // ignore transparent blocks
            );
            
            if (!hasLOS) {
                // Если LOS заблокирован, пропустить эту сущность
                continue;
            }
            
            // deal damage once per shard lifetime
            if (!damageDealt) {
                float damage = (config != null) ? config.getShardDamage() : 0.5f;
                entity.attackEntityFrom(DamageSource.GENERIC, damage);
                
                // knockback
                entity.addVelocity(
                    this.motionX * 0.1,
                    this.motionY * 0.05,
                    this.motionZ * 0.1
                );
                
                // play glass break sound
                this.world.playSound(null, this.posX, this.posY, this.posZ, 
                    net.minecraft.init.SoundEvents.BLOCK_GLASS_BREAK, 
                    net.minecraft.util.SoundCategory.BLOCKS, 0.5f, 1.0f);
                
                damageDealt = true;
            }
            
            // shard self-destructs on contact
            this.dropAsItem();
            this.setDead();
            return;
        }
    }
    
    /**
     * Convert shard to item entity.
     */
    private void dropAsItem() {
        if (this.world.isRemote) {
            return;
        }
        
        if (config != null && !config.isItemDropEnabled()) {
            return; // shard vanishes if item drop disabled
        }
        
        // play glass break sound when dropping as item
        this.world.playSound(null, this.posX, this.posY, this.posZ, 
            net.minecraft.init.SoundEvents.BLOCK_GLASS_BREAK, 
            net.minecraft.util.SoundCategory.BLOCKS, 0.3f, 1.0f);
        
        ItemStack itemStack = new ItemStack(Blocks.GLASS);
        EntityItem itemEntity = new EntityItem(
            this.world,
            this.posX,
            this.posY,
            this.posZ,
            itemStack
        );
        
        // inherit velocity
        itemEntity.motionX = this.motionX;
        itemEntity.motionY = this.motionY;
        itemEntity.motionZ = this.motionZ;
        
        // item can't be picked up immediately (avoid abuse)
        itemEntity.setPickupDelay(10);
        
        this.world.spawnEntity(itemEntity);
    }
    
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.age = compound.getInteger("age");
        this.lifetime = compound.getInteger("lifetime");
        this.hasHitGround = compound.getBoolean("hasHitGround");
        this.damageDealt = compound.getBoolean("damageDealt");
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("age", this.age);
        compound.setInteger("lifetime", this.lifetime);
        compound.setBoolean("hasHitGround", this.hasHitGround);
        compound.setBoolean("damageDealt", this.damageDealt);
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }
    
    @Override
    public boolean canBePushed() {
        return !this.isDead;
    }
    
}
