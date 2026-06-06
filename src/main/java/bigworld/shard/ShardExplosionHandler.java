package bigworld.shard;

import bigworld.IShardProvider;
import bigworld.BWRCore;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Example event handler showing how to integrate shard creation with explosion events.
 * 
 * This is NOT auto-registered by BigWorld.
 * Client mods must manually register this event listener if desired:
 * 
 *   MinecraftForge.EVENT_BUS.register(new ShardExplosionHandler());
 * 
 * Or implement their own custom integration logic.
 */
public class ShardExplosionHandler {
    
    /**
     * Hook into explosion events (Detonate phase).
     * Only triggers after explosion has been detonated (blocks already broken).
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        IShardProvider shardProvider = BWRCore.getShardProvider();
        if (shardProvider == null) {
            return; // shard system not initialized
        }
        
        // create shards from explosion center with estimated radius (4.0 is typical TNT explosion)
        shardProvider.createShardsFromExplosion(
            event.getWorld(),
            event.getExplosion().getPosition(),
            4.0f, // default explosion radius
            1.0f // normal impulse
        );
    }
    
    /**
     * Example of creating shards manually on a block break event.
     * This shows the pattern for other event types (BlockEvent.BreakEvent, etc.)
     */
    public static void onGlassBlockBreak(BlockPos blockPos, BlockPos explosiveSource) {
        IShardProvider shardProvider = BWRCore.getShardProvider();
        if (shardProvider == null) {
            return;
        }
        
        // manually trigger shard creation
        shardProvider.createShardsOnBlockBreak(
            null, // TODO: get world from context
            blockPos,
            explosiveSource,
            -1, // use default count from config
            1.0f // normal impulse
        );
    }
    
}
