package com.coders.explosion;

import net.minecraft.block.BlockTNT;
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

    private static final int BASE_RADIUS = 8; // базовый радиус поражения TNT

    @SubscribeEvent
    public void onTNTExplosion(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        Explosion explosion = event.getExplosion();
        Vec3d center = explosion.getPosition();

        Set<BlockPos> tntToExplode = new HashSet<>();

        // Собираем все TNT в радиусе BASE_RADIUS
        int radius = BASE_RADIUS;
        BlockPos centerPos = new BlockPos(center);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centerPos.add(dx, dy, dz);
                    if (world.getBlockState(pos).getBlock() == Blocks.TNT) {
                        tntToExplode.add(pos);
                    }
                }
            }
        }

        // Увеличиваем радиус взрыва пропорционально количеству TNT
        int finalRadius = BASE_RADIUS + tntToExplode.size() * 2;

        // Взрываем все TNT одновременно
        for (BlockPos pos : tntToExplode) {
            if (!world.isRemote) {
                world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        finalRadius, true);
                world.setBlockToAir(pos); // удаляем TNT после взрыва
            }
        }
    }
}
