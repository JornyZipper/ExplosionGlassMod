package bigworld.los;

import bigworld.ILOSProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;

/**
 * Manual LOS service implementing `ILOSProvider`.
 * No automatic registration or event subscriptions — client mods instantiate and call directly.
 */
public final class LOSManager implements ILOSProvider {
    private final LOSCache cache;

    /**
     * Create a LOS service. Pass useCache=true to enable an internal primitive cache.
     */
    public LOSManager(boolean useCache) {
        this.cache = useCache ? new LOSCache() : null;
    }

    public LOSManager() {
        this(false);
    }

    public void clearCache() {
        if (cache != null) cache.clear();
    }

    @Override
    public boolean hasLineOfSight(World world, BlockPos from, BlockPos to, boolean ignoreTransparent) {
        if (world == null || from == null || to == null) return true;

        long key = 0L;
        if (cache != null) {
            key = packKey(world.provider.getDimension(), from, to, ignoreTransparent);
            if (cache.containsKey(key)) return cache.get(key);
        }

        boolean result = computeLOS(world, from, to, ignoreTransparent);

        if (cache != null) cache.put(key, result);
        return result;
    }

    private static long packPos(BlockPos p) {
        long x = (long) p.getX() & 0xFFFFFFFFL;
        long y = (long) p.getY() & 0xFFFFL;
        long z = (long) p.getZ() & 0xFFFFFFFFL;
        return (x << 32) ^ (y << 16) ^ z;
    }

    private static long packKey(int dim, BlockPos a, BlockPos b, boolean f) {
        long pa = packPos(a);
        long pb = packPos(b);
        long k = pa ^ (pb << 1) ^ ((long) dim << 48) ^ (f ? 0x8000000000000000L : 0L);
        return k;
    }

    private boolean computeLOS(World world, BlockPos from, BlockPos to, boolean ignoreTransparent) {
        int x1 = from.getX();
        int y1 = from.getY();
        int z1 = from.getZ();
        int x2 = to.getX();
        int y2 = to.getY();
        int z2 = to.getZ();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);

        int xs = x2 > x1 ? 1 : -1;
        int ys = y2 > y1 ? 1 : -1;
        int zs = z2 > z1 ? 1 : -1;

        int x = x1;
        int y = y1;
        int z = z1;

        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;
            for (int i = 0; i <= dx; i++) {
                if (blocksLOS(world, x, y, z, from, to, ignoreTransparent)) return false;
                x += xs;
                if (p1 > 0) { y += ys; p1 -= 2 * dx; }
                if (p2 > 0) { z += zs; p2 -= 2 * dx; }
                p1 += 2 * dy; p2 += 2 * dz;
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;
            for (int i = 0; i <= dy; i++) {
                if (blocksLOS(world, x, y, z, from, to, ignoreTransparent)) return false;
                y += ys;
                if (p1 > 0) { x += xs; p1 -= 2 * dy; }
                if (p2 > 0) { z += zs; p2 -= 2 * dy; }
                p1 += 2 * dx; p2 += 2 * dz;
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;
            for (int i = 0; i <= dz; i++) {
                if (blocksLOS(world, x, y, z, from, to, ignoreTransparent)) return false;
                z += zs;
                if (p1 > 0) { y += ys; p1 -= 2 * dz; }
                if (p2 > 0) { x += xs; p2 -= 2 * dz; }
                p1 += 2 * dy; p2 += 2 * dx;
            }
        }

        return true;
    }

    private boolean blocksLOS(World world, int x, int y, int z, BlockPos from, BlockPos to, boolean ignoreTransparent) {
        if ((x == from.getX() && y == from.getY() && z == from.getZ()) ||
            (x == to.getX() && y == to.getY() && z == to.getZ())) return false;

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        boolean air = world.isAirBlock(pos);
        if (air) return false;

        if (ignoreTransparent) {
            // only opaque blocks block LOS
            return state.isOpaqueCube();
        } else {
            // any non-air blocks block LOS
            return true;
        }
    }
}

