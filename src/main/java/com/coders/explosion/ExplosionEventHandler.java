package com.coders.explosion;

import com.coders.explosion.ExplosionGlassMod;
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
        if (!ExplosionGlassMod.Mod) {
            return;
        }

        World world = event.getWorld();
        Vec3d explosionPos = event.getExplosion().getPosition();
        int radius = ExplosionGlassMod.glassBreakRadius;
        BlockPos center = new BlockPos(explosionPos);

        for (BlockPos pos : BlockPos.getAllInBox(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))) {
            IBlockState state = world.getBlockState(pos);
            Material material = state.getMaterial();
            ResourceLocation blockRL = state.getBlock().getRegistryName();

            // Проверка: пустота, стекло в blacklist или не стекло
            if (world.isAirBlock(pos)
                    || material != Material.GLASS
                    || (blockRL != null && Arrays.asList(ExplosionGlassMod.glassBlacklist).contains(blockRL.toString()))) {
                continue;
            }

            Vec3d glassCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = explosionPos.distanceTo(glassCenter);
            boolean hasLoS = true;

            if (ExplosionGlassMod.useLineOfSight) {
                hasLoS = false;
                outer:
                for (double dx = 0.0; dx <= 1.0; dx += 1.0) {
                    for (double dy = 0.0; dy <= 1.0; dy += 1.0) {
                        for (double dz = 0.0; dz <= 1.0; dz += 1.0) {
                            Vec3d corner = new Vec3d(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                            RayTraceResult result = world.rayTraceBlocks(explosionPos, corner, false, true, false);
                            if (result != null && !result.getBlockPos().equals(pos)) {
                                continue;
                            }
                            hasLoS = true;
                            break outer;
                        }
                    }
                }
            }

            if (!hasLoS || distance > radius) continue;

            // Разрушение блока
            world.setBlockToAir(pos);

            // Дропы
            if (!ExplosionGlassMod.glassDrops) continue;

            Block block = state.getBlock();
            ItemStack drop = ItemStack.EMPTY;

            if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS) {
                drop = new ItemStack(block, 1, block.getMetaFromState(state));
            } else if (block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS_PANE) {
                drop = new ItemStack(block, 1, block.getMetaFromState(state));
            }

            if (drop.isEmpty()) continue;

            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
        }
    }
}
