package com.coders.explosion;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class ChainTNTHandler {

    private static final int BASE_RADIUS = 8;

    @SubscribeEvent
    public void onTNTExplosion(ExplosionEvent.Detonate event) {
        World world = event.getWorld();

        if (world.isRemote) {
            return;
        }

        Explosion explosion = event.getExplosion();
        Vec3d center = explosion.getPosition();

        final Set<BlockPos> tntToExplode = new HashSet<>();

        final int radius = BASE_RADIUS;
        final BlockPos centerPos = new BlockPos(center);
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {

                    pos.setPos(
                            centerPos.getX() + dx,
                            centerPos.getY() + dy,
                            centerPos.getZ() + dz
                    );

                    if (world.getBlockState(pos).getBlock() == Blocks.TNT) {
                        tntToExplode.add(new BlockPos(pos));
                    }
                }
            }
        }

        if (tntToExplode.isEmpty()) {
            return;
        }

        final int finalRadius = BASE_RADIUS + tntToExplode.size() * 2;

        for (BlockPos tntPos : tntToExplode) {
            world.createExplosion(
                    null,
                    tntPos.getX() + 0.5,
                    tntPos.getY() + 0.5,
                    tntPos.getZ() + 0.5,
                    finalRadius,
                    true
            );
            world.setBlockToAir(tntPos);
        }
    }
}