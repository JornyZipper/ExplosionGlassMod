package bigworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Public API: Line-of-Sight provider interface.
 * Implementations are instantiated and used by client mods; BigWorld does not auto-register them.
 */
public interface ILOSProvider {
    /**
     * Return true if there is an unobstructed line of sight between two block positions in the given world.
     * @param world world instance
     * @param from source block position
     * @param to target block position
     * @param ignoreTransparent if true, transparent blocks (non-opaque) are ignored and do not block LOS
     */
    boolean hasLineOfSight(World world, BlockPos from, BlockPos to, boolean ignoreTransparent);
}
