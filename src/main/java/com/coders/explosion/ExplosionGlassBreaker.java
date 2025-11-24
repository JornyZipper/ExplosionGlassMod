package com.coders.explosion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ExplosionGlassBreaker {

    private static final int BASE_RADIUS = 16; // базовый радиус 1 TNT
    private static final int MAX_RADIUS = 64;  // максимальный радиус

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        Vec3d explosionPos = event.getExplosion().getPosition();
        BlockPos center = new BlockPos(explosionPos);

        // 1. Собираем все TNT в радиусе BASE_RADIUS
        List<BlockPos> tntList = new ArrayList<>();
        for (BlockPos pos : BlockPos.getAllInBox(center.add(-BASE_RADIUS, -BASE_RADIUS, -BASE_RADIUS),
                center.add(BASE_RADIUS, BASE_RADIUS, BASE_RADIUS))) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof BlockTNT) {
                tntList.add(pos);
            }
        }

        // 2. Считаем радиус в зависимости от количества TNT
        int radius = Math.min(MAX_RADIUS, BASE_RADIUS + tntList.size() * 8);

        // 3. Разрушаем стекло в радиусе с улучшенным LoS
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos currentPos = center.add(dx, dy, dz);
                    Block block = world.getBlockState(currentPos).getBlock();

                    if (!isGlassBlock(block)) continue;
                    if (!hasLineOfSight(world, explosionPos, currentPos)) continue;

                    // Разрушаем блок
                    world.setBlockToAir(currentPos);

                    // Дроп стекла
                    ItemStack drop = new ItemStack(block, 1, block.getMetaFromState(world.getBlockState(currentPos)));
                    if (!drop.isEmpty()) {
                        InventoryHelper.spawnItemStack(world, currentPos.getX(), currentPos.getY(), currentPos.getZ(), drop);
                    }
                }
            }
        }

        // 4. Взрываем все TNT в списке одновременно
        for (BlockPos tntPos : tntList) {
            world.createExplosion(null, tntPos.getX(), tntPos.getY(), tntPos.getZ(), radius, true);
            world.setBlockToAir(tntPos); // убираем блок TNT
        }
    }

    private boolean hasLineOfSight(World world, Vec3d from, BlockPos pos) {
        Vec3d[] points = new Vec3d[] {
                new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ()),
                new Vec3d(pos.getX(), pos.getY() + 1, pos.getZ()),
                new Vec3d(pos.getX(), pos.getY(), pos.getZ() + 1),
                new Vec3d(pos.getX() + 1, pos.getY() + 1, pos.getZ()),
                new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ() + 1),
                new Vec3d(pos.getX(), pos.getY() + 1, pos.getZ() + 1),
                new Vec3d(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
        };

        for (Vec3d point : points) {
            RayTraceResult result = world.rayTraceBlocks(from, point, false, true, false);
            if (result == null || result.getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGlassBlock(Block block) {
        return block == Blocks.GLASS
                || block == Blocks.STAINED_GLASS
                || block == Blocks.GLASS_PANE
                || block == Blocks.STAINED_GLASS_PANE;
    }
}
