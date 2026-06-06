package bigworld.los;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Filter used by LOS checks to decide if a block position should be treated as transparent.
 */
public interface LOSFilter {
    /**
     * Return true if the block at `pos` in `world` should NOT block line-of-sight.
     */
    boolean isTransparent(World world, BlockPos pos);
}
