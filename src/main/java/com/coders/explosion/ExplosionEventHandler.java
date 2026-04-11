package com.coders.explosion;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
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
        System.out.println("[ExplosionGlass] Explosion detected at: " + explosionPos);

        // Scale radii by explosion strength: stronger explosions expand effect
        int radiusNoLoS = ExplosionGlassMod.glassBreakRadius;
        int radiusLoS = ExplosionGlassMod.glassBreakRadiusWithLoS;

        // Try to read explosion size (Minecraft Explosion has a 'size' field in 1.12.2)
        float explosionSize = 0f;
        try {
            java.lang.reflect.Field sizeField = event.getExplosion().getClass().getDeclaredField("size");
            sizeField.setAccessible(true);
            explosionSize = sizeField.getFloat(event.getExplosion());
        } catch (Exception e) {
            // Fallback: try common field name 'explosionSize' or leave as 0
            try {
                java.lang.reflect.Field sizeField = event.getExplosion().getClass().getDeclaredField("explosionSize");
                sizeField.setAccessible(true);
                explosionSize = sizeField.getFloat(event.getExplosion());
            } catch (Exception ignored) {
            }
        }

        // If we found a meaningful explosion size, scale radii relative to typical TNT (~4.0f)
        if (explosionSize > 0f) {
            float scale = Math.max(1.0f, explosionSize / 4.0f);
            radiusNoLoS = (int) Math.ceil(radiusNoLoS * scale);
            radiusLoS = (int) Math.ceil(radiusLoS * scale);
        }
        double ignoreDistance = ExplosionGlassMod.loSIgnoreDistance;

        // Use full 3D bounding box based on computed radii so vertical range matches horizontal
        int verticalRange = Math.max(radiusNoLoS, radiusLoS);
        for (BlockPos pos : BlockPos.getAllInBox(
                new BlockPos(explosionPos).add(-radiusNoLoS, -verticalRange, -radiusNoLoS),
                new BlockPos(explosionPos).add(radiusNoLoS, verticalRange, radiusNoLoS))) {

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
                String blockType = determineBlockType(state);
                breakGlass(world, pos, state, explosionPos, blockType, explosionSize);
                continue;
            }

            // Для обычного стекла и льда проверяем материал и радиус
            boolean isGlass = material == Material.GLASS;
            boolean isIce = material == Material.ICE;

            if (!isGlass && !isIce) continue;

            System.out.println("[ExplosionGlass] Found " + (isGlass ? "glass" : "ice") + " block at: " + pos + " (material: " + material + ")");

            // Центр блока для расчетов расстояния
            Vec3d glassCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = explosionPos.distanceTo(glassCenter);

            // Всегда разбиваем стекло/лёд в radiusNoLoS (без LoS проверки)
            // Но если loSIgnoreDistance > 0, эта зона сокращается до ignoreDistance
            double actualNoLosRadius = ignoreDistance > 0 ? ignoreDistance : radiusNoLoS;
            if (distance <= actualNoLosRadius) {
                breakGlass(world, pos, state, explosionPos, isGlass ? "glass" : "ice", explosionSize);
                continue;
            }

            // Если LoS включен - проверяем видимость через BWR-Core ILOS provider
            if (ExplosionGlassMod.useLineOfSight && distance <= radiusLoS) {
                if (com.coders.explosion.bwr.BwrLosBridge.canSee(world, explosionPos, pos)) {
                    breakGlass(world, pos, state, explosionPos, isGlass ? "glass" : "ice", explosionSize);
                }
            }
        }
    }

    // Проверка прямой видимости - проверяем есть ли непрозрачные блоки в пути
    // LoS implementation removed; BWR-Core ILOS provider is used instead via BwrLosBridge.

    // Определить тип блока (стекло или лед) на основе состояния блока
    private String determineBlockType(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();

        if (material == Material.ICE) {
            return "ice";
        } else {
            return "glass";
        }
    }

    // Метод для разрушения блока и спавна дропа
    private void breakGlass(World world, BlockPos pos, IBlockState state, Vec3d explosionPos, String blockType, float explosionSize) {
        System.out.println("[ExplosionGlass] === Breaking " + blockType + " block at: " + pos + " ===");

        // Play break sound server-side
        if (!world.isRemote) {
            world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_GLASS_BREAK,
                    net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        // Remove block from world
        world.setBlockToAir(pos);
        System.out.println("[ExplosionGlass] Block removed from world at: " + pos);

        // Spawn item drop if configured
        if (ExplosionGlassMod.glassDrops) {
            Block block = state.getBlock();
            ItemStack drop = ItemStack.EMPTY;

            if (blockType.equals("glass")) {
                if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS
                        || block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS_PANE) {
                    drop = new ItemStack(block, 1, block.getMetaFromState(state));
                }
            } else if (blockType.equals("ice")) {
                if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.FROSTED_ICE) {
                    drop = new ItemStack(block, 1, block.getMetaFromState(state));
                }
            }

            if (!drop.isEmpty() && Math.random() <= ExplosionGlassMod.glassDropChance) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }
    }
}
