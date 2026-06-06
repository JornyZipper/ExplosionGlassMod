package com.coders.explosion.bwr;

import bigworld.los.LOSManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class BwrLosBridge {
    private BwrLosBridge() {}

    /**
     * Ask embedded BigWorld LOS service whether the explosion source can see the target block.
     */
    public static boolean canSee(World world, Vec3d from, BlockPos targetPos) {
        if (world == null || from == null || targetPos == null) return false;

        try {
            LOSManager manager = new LOSManager(true);
            return manager.hasLineOfSight(world, new BlockPos(from), targetPos, true);
        } catch (Throwable t) {
            System.out.println("BwrLosBridge: error while invoking embedded LOS manager: " + t.getMessage());
            return false;
        }
    }
}
