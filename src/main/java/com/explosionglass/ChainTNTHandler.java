package com.explosionglass;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class ChainTNTHandler {

    private static final int BASE_RADIUS = 8; // базовый радиус поражения TNT

    @SubscribeEvent
    public void onTNTExplosion(ExplosionEvent.Detonate event) {
        Level world = event.getLevel();
        Vec3 center = getExplosionPosition(event.getExplosion());

        Set<BlockPos> tntToExplode = new HashSet<>();

        // Собираем все TNT в радиусе BASE_RADIUS
        int radius = BASE_RADIUS;
        BlockPos centerPos = new BlockPos((int)center.x, (int)center.y, (int)center.z);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centerPos.offset(dx, dy, dz);
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
            if (!world.isClientSide) {
                world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        finalRadius, true, Level.ExplosionInteraction.TNT);
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3); // удаляем TNT после взрыва
            }
        }
    }

    private Vec3 getExplosionPosition(Explosion explosion) {
        try {
            java.lang.reflect.Field positionField = explosion.getClass().getDeclaredField("position");
            positionField.setAccessible(true);
            Object value = positionField.get(explosion);
            if (value instanceof Vec3 vec3) {
                return vec3;
            }
        } catch (Exception ignored) {
        }

        try {
            double x = getExplosionDoubleField(explosion, "x");
            double y = getExplosionDoubleField(explosion, "y");
            double z = getExplosionDoubleField(explosion, "z");
            return new Vec3(x, y, z);
        } catch (Exception ignored) {
        }

        try {
            java.lang.reflect.Method method = explosion.getClass().getDeclaredMethod("getPosition");
            method.setAccessible(true);
            Object result = method.invoke(explosion);
            if (result instanceof Vec3 vec3) {
                return vec3;
            }
        } catch (Exception ignored) {
        }

        return Vec3.ZERO;
    }

    private double getExplosionDoubleField(Explosion explosion, String fieldName) throws Exception {
        java.lang.reflect.Field field = explosion.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(explosion);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }
}