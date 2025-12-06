package com.coders.explosion;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExplosionEventHandler {

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        if (!ExplosionGlassMod.Mod) return;

        World world = event.getWorld();
        Vec3d explosionPos = event.getExplosion().getPosition();

        int radiusNoLoS = ExplosionGlassMod.glassBreakRadius;
        int radiusLoS = ExplosionGlassMod.glassBreakRadiusWithLoS;
        double ignoreDistance = ExplosionGlassMod.loSIgnoreDistance;

        for (BlockPos pos : BlockPos.getAllInBox(
                new BlockPos(explosionPos).add(-radiusNoLoS, -2, -radiusNoLoS),
                new BlockPos(explosionPos).add(radiusNoLoS, 5, radiusNoLoS))) {

            IBlockState state = world.getBlockState(pos);
            Material material = state.getMaterial();
            ResourceLocation blockRL = state.getBlock().getRegistryName();
            String blockName = blockRL != null ? blockRL.toString() : "";

            // Пропускаем пустые блоки и воздух
            if (world.isAirBlock(pos)) continue;

            boolean isBlacklisted = Arrays.asList(ExplosionGlassMod.glassBlacklist).contains(blockName);
            boolean isWhitelisted = Arrays.asList(ExplosionGlassMod.glassWhitelist).contains(blockName);

            // Если блок в blacklist — точно не ломаем
            if (isBlacklisted) continue;

            // Если блок в whitelist — ломаем сразу, минуя LoS и радиус
            if (isWhitelisted) {
                breakGlass(world, pos, state);
                continue;
            }

            // Для обычного стекла проверяем материал и радиус
            if (material != Material.GLASS) continue;

            // Центр блока для расчетов расстояния
            Vec3d glassCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = explosionPos.distanceTo(glassCenter);

            // ВСЕГДА разбиваем стекло в radiusNoLoS (без LoS проверки)
            // Но если loSIgnoreDistance > 0, эта зона сокращается до ignoreDistance
            double actualNoLosRadius = ignoreDistance > 0 ? ignoreDistance : radiusNoLoS;
            if (distance <= actualNoLosRadius) {
                breakGlass(world, pos, state);
                continue;
            }

            // Если LoS включен - проверяем видимость до целевого блока в radiusLoS
            if (ExplosionGlassMod.useLineOfSight && distance <= radiusLoS) {
                if (canSeeTarget(world, explosionPos, pos)) {
                    breakGlass(world, pos, state);
                }
            }
        }
    }

    // Проверка прямой видимости - проверяем есть ли непрозрачные блоки в пути
    private boolean canSeeTarget(World world, Vec3d from, BlockPos targetPos) {
        // Проверяем 8 углов целевого блока - если хотя бы один видим, есть LoS
        for (double dx = 0.0; dx <= 1.0; dx += 1.0) {
            for (double dy = 0.0; dy <= 1.0; dy += 1.0) {
                for (double dz = 0.0; dz <= 1.0; dz += 1.0) {
                    Vec3d corner = new Vec3d(
                        (double)targetPos.getX() + dx, 
                        (double)targetPos.getY() + dy, 
                        (double)targetPos.getZ() + dz
                    );
                    
                    // Проверяем видимость - есть ли прозрачный путь до этого угла
                    if (isPathClear(world, from, corner, targetPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Проверяет есть ли прозрачный путь от from до to (игнорируя сам целевой блок)
    private boolean isPathClear(World world, Vec3d from, Vec3d to, BlockPos targetPos) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Проверяем каждый блок на пути
        int steps = (int) (distance * 2) + 1;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3d point = new Vec3d(
                from.x + dx * t,
                from.y + dy * t,
                from.z + dz * t
            );
            
            BlockPos checkPos = new BlockPos(point);
            
            // Пропускаем сам целевой блок и исходную позицию
            if (checkPos.equals(targetPos) || checkPos.equals(new BlockPos(from))) {
                continue;
            }
            
            IBlockState state = world.getBlockState(checkPos);
            Material material = state.getMaterial();
            
            // Если блок непрозрачен и не воздух - путь заблокирован
            if (!state.getBlock().isAir(state, world, checkPos) && 
                material != Material.GLASS && 
                material != Material.WATER && 
                material != Material.LEAVES &&
                material != Material.VINE) {
                return false;
            }
        }
        
        return true;
    }

    // Метод для разрушения блока и спавна дропа
    private void breakGlass(World world, BlockPos pos, IBlockState state) {
        world.setBlockToAir(pos);

        if (ExplosionGlassMod.glassDrops) {
            Block block = state.getBlock();
            ItemStack drop = ItemStack.EMPTY;

            if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS
                    || block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS_PANE) {
                drop = new ItemStack(block, 1, block.getMetaFromState(state));
            }

            if (!drop.isEmpty() && Math.random() <= ExplosionGlassMod.glassDropChance) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }
    }
}
