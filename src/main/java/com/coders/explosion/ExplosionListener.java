package com.coders.explosion;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExplosionListener {

    private static final Random rand = new Random();

    private static boolean isFragileBlock(Block block) {
        return block instanceof BlockGlass
                || block == Blocks.GLASS
                || block == Blocks.STAINED_GLASS
                || block == Blocks.GLASS_PANE
                || block == Blocks.STAINED_GLASS_PANE;
    }

    private static boolean isDoorBlock(Block block) {
        return block instanceof BlockDoor
                || block == Blocks.OAK_DOOR
                || block == Blocks.IRON_DOOR;
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        Explosion explosion = event.getExplosion();
        Vec3d expPos = explosion.getPosition();
        BlockPos center = new BlockPos(expPos);

        // Получаем силу взрыва через рефлексию, так безопаснее для 1.12.2
        float strength = 4.0f;
        try {
            java.lang.reflect.Field field = Explosion.class.getDeclaredField("explosionSize"); // internal name
            field.setAccessible(true);
            strength = field.getFloat(explosion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int radius = Math.min(64, Math.max(1, (int)(strength * 4)));

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos current = center.add(dx, dy, dz);
                    IBlockState state = world.getBlockState(current);
                    Block block = state.getBlock();

                    if (isFragileBlock(block)) {
                        if (hasLineOfSight(world, expPos, current)) {
                            world.setBlockToAir(current);
                        }
                        continue;
                    }

                    if (isDoorBlock(block) && block instanceof BlockDoor) {
                        BlockDoor door = (BlockDoor) block;
                        IProperty doorOpenProp = BlockDoor.OPEN;
                        IProperty doorHalfProp = BlockDoor.HALF;

                        if (state.getValue(doorHalfProp) == BlockDoor.EnumDoorHalf.LOWER) {
                            double distanceSq = center.distanceSq(current);
                            if (distanceSq <= 225.0 && distanceSq > 100.0) {
                                world.setBlockToAir(current);
                            } else if (distanceSq <= 900.0 && distanceSq > 225.0 && rand.nextFloat() < 0.5f) {
                                boolean isOpen = state.getValue((IProperty<Boolean>) doorOpenProp);
                                world.setBlockState(current, state.withProperty((IProperty<Boolean>) doorOpenProp, !isOpen), 2);
                            }
                        }
                    }
                }
            }
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
            if (world.rayTraceBlocks(from, point, false, true, false) == null) {
                return true;
            }
        }
        return false;
    }
}
