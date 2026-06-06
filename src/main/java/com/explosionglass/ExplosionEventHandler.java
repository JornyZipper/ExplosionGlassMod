package com.explosionglass;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

import java.util.List;
import java.util.Random;

public class ExplosionEventHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!Config.enabled || Config.handBreakShardCount <= 0) {
            return;
        }

        if (!(event.getLevel() instanceof Level world)) {
            return;
        }
        if (world.isClientSide) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isCreative()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        boolean isGlass = isGlassBlock(state);
        boolean isIce = isIceBlock(state);
        if (!isGlass && !isIce) {
            return;
        }

        String blockType = determineBlockType(state);
        SoundEvent sound = SoundRegistry.random(blockType.equals("glass") ? SoundRegistry.getGlassBreakSounds() : SoundRegistry.getIceBreakSounds());
        if (sound == null) {
            sound = net.minecraft.sounds.SoundEvents.GLASS_BREAK;
        }
        world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);

        spawnShardEntities(world, pos, state, new Vec3(player.getX(), player.getY(), player.getZ()), Config.handBreakShardCount);
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        if (!Config.enabled) return;

        Level world = event.getLevel();
        Vec3 explosionPos = getExplosionPosition(event.getExplosion());
        System.out.println("[ExplosionGlass] Explosion detected at: " + explosionPos);

        // Scale radii by explosion strength: stronger explosions expand effect
        int radiusNoLoS = Config.glassBreakRadius;
        int radiusLoS = Config.glassBreakRadiusWithLoS;

        float explosionSize = getExplosionSize(event.getExplosion());

        // If we found a meaningful explosion size, scale radii relative to typical TNT (~4.0f)
        if (explosionSize > 0f) {
            float scale = Math.max(1.0f, explosionSize / 4.0f);
            radiusNoLoS = (int) Math.ceil(radiusNoLoS * scale);
            radiusLoS = (int) Math.ceil(radiusLoS * scale);
        }
        double ignoreDistance = Config.loSIgnoreDistance;

        // Use full 3D bounding box based on computed radii so vertical range matches horizontal
        int verticalRange = Math.max(radiusNoLoS, radiusLoS);
        BlockPos minPos = new BlockPos((int)explosionPos.x - radiusNoLoS, (int)explosionPos.y - verticalRange, (int)explosionPos.z - radiusNoLoS);
        BlockPos maxPos = new BlockPos((int)explosionPos.x + radiusNoLoS, (int)explosionPos.y + verticalRange, (int)explosionPos.z + radiusNoLoS);

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {

            BlockState state = world.getBlockState(pos);
            ResourceLocation blockRL = state.getBlock().builtInRegistryHolder().key().location();
            String blockName = blockRL != null ? blockRL.toString() : "";

            // Пропускаем пустые блоки и воздух
            if (world.isEmptyBlock(pos)) continue;

            boolean isBlacklisted = Config.glassBlacklist.contains(blockName);
            boolean isWhitelisted = Config.glassWhitelist.contains(blockName);

            // Если блок в blacklist — точно не ломаем
            if (isBlacklisted) continue;

            // Если блок в whitelist — ломаем сразу, минуя LoS и радиус
            if (isWhitelisted) {
                String blockType = determineBlockType(state);
                breakGlass(world, pos, state, explosionPos, blockType, explosionSize);
                continue;
            }

            // Для обычного стекла и льда проверяем блок напрямую
            boolean isGlass = isGlassBlock(state);
            boolean isIce = isIceBlock(state);

            if (!isGlass && !isIce) continue;

            System.out.println("[ExplosionGlass] Found " + (isGlass ? "glass" : "ice") + " block at: " + pos);

            // Центр блока для расчетов расстояния
            Vec3 glassCenter = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = explosionPos.distanceTo(glassCenter);

            // Всегда разбиваем стекло/лёд в radiusNoLoS (без LoS проверки)
            // Но если loSIgnoreDistance > 0, эта зона сокращается до ignoreDistance
            double actualNoLosRadius = ignoreDistance > 0 ? ignoreDistance : radiusNoLoS;
            if (distance <= actualNoLosRadius) {
                breakGlass(world, pos, state, explosionPos, isGlass ? "glass" : "ice", explosionSize);
                continue;
            }

            // If using per-block LoS checks (legacy) - keep old behaviour
            if (!Config.use3DWave) {
                // Если LoS включен - проверяем видимость
                if (Config.useLineOfSight && distance <= radiusLoS) {
                    if (canSee(world, explosionPos, glassCenter)) {
                        breakGlass(world, pos, state, explosionPos, isGlass ? "glass" : "ice", explosionSize);
                    }
                }
            }
        }
        
        // If configured, do a spherical 3D wave raycast to break visible glass within LoS radius
        if (Config.use3DWave && Config.useLineOfSight) {
            propagate3DWave(world, explosionPos, radiusLoS, explosionSize);
        }

    }

    // Basic spherical raycasting wave: cast rays in sampled directions and break first glass/ice blocks encountered along each ray
    private void propagate3DWave(Level world, Vec3 origin, int radiusLoS, float explosionSize) {
        if (world.isClientSide) return;

        int latSteps = 8; // polar steps
        int lonSteps = 16; // azimuthal steps
        double step = 0.5; // step along ray in blocks

        for (int i = 0; i < latSteps; i++) {
            double theta = Math.PI * (i + 0.5) / latSteps; // polar angle 0..pi
            for (int j = 0; j < lonSteps; j++) {
                double phi = 2.0 * Math.PI * j / lonSteps; // azimuth 0..2pi

                // Direction vector in spherical coords
                double dx = Math.sin(theta) * Math.cos(phi);
                double dy = Math.cos(theta);
                double dz = Math.sin(theta) * Math.sin(phi);

                Vec3 dir = new Vec3(dx, dy, dz);

                for (double t = 0; t <= radiusLoS; t += step) {
                    Vec3 sample = origin.add(dir.scale(t));
                    BlockPos pos = new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y), (int) Math.floor(sample.z));

                    if (pos.getY() < world.getMinBuildHeight() || pos.getY() > world.getMaxBuildHeight()) break;

                    if (world.isEmptyBlock(pos)) continue;

                    BlockState state = world.getBlockState(pos);
                    ResourceLocation blockRL = state.getBlock().builtInRegistryHolder().key().location();
                    String blockName = blockRL != null ? blockRL.toString() : "";

                    if (Config.glassBlacklist.contains(blockName)) {
                        // stop ray when hitting blacklisted solid block
                        if (!state.getCollisionShape(world, pos, CollisionContext.empty()).isEmpty()) break;
                        continue;
                    }

                    if (Config.glassWhitelist.contains(blockName)) {
                        breakGlass(world, pos, state, origin, determineBlockType(state), explosionSize);
                        // stop ray after breaking this block
                        break;
                    }

                    if (isGlassBlock(state) || isIceBlock(state)) {
                        breakGlass(world, pos, state, origin, isGlassBlock(state) ? "glass" : "ice", explosionSize);
                        // break only the first glass/ice encountered along this ray
                        break;
                    }

                    // if this is an opaque/solid block it stops the ray
                    if (!state.getCollisionShape(world, pos, CollisionContext.empty()).isEmpty()) break;
                }
            }
        }
    }
    // Проверка прямой видимости
    private boolean canSee(Level world, Vec3 from, Vec3 to) {
        // Simple line-of-sight check
        try {
            net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(from, to, 
                    net.minecraft.world.level.ClipContext.Block.COLLIDER, 
                    net.minecraft.world.level.ClipContext.Fluid.NONE, (net.minecraft.world.phys.shapes.CollisionContext) CollisionContext.empty());
            HitResult result = world.clip(context);
            
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) result;
                BlockPos hitPos = blockHit.getBlockPos();
                BlockPos targetPos = new BlockPos((int)to.x, (int)to.y, (int)to.z);
                return hitPos.equals(targetPos) || hitPos.closerThan(targetPos, 2);
            }
        } catch (Exception e) {
            System.out.println("[ExplosionGlass] LoS check error: " + e.getMessage());
        }
        return true;
    }

    // Определить тип блока (стекло или лед) на основе состояния блока
    private String determineBlockType(BlockState state) {
        Block block = state.getBlock();
        
        if (isIceBlock(state)) {
            return "ice";
        } else {
            return "glass";
        }
    }

    // Проверить является ли блок стеклом
    private boolean isGlassBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GLASS ||
               block == Blocks.GLASS_PANE ||
               block == Blocks.WHITE_STAINED_GLASS ||
               block == Blocks.WHITE_STAINED_GLASS_PANE ||
               block == Blocks.ORANGE_STAINED_GLASS ||
               block == Blocks.MAGENTA_STAINED_GLASS ||
               block == Blocks.LIGHT_BLUE_STAINED_GLASS ||
               block == Blocks.YELLOW_STAINED_GLASS ||
               block == Blocks.LIME_STAINED_GLASS ||
               block == Blocks.PINK_STAINED_GLASS ||
               block == Blocks.GRAY_STAINED_GLASS ||
               block == Blocks.LIGHT_GRAY_STAINED_GLASS ||
               block == Blocks.CYAN_STAINED_GLASS ||
               block == Blocks.PURPLE_STAINED_GLASS ||
               block == Blocks.BLUE_STAINED_GLASS ||
               block == Blocks.BROWN_STAINED_GLASS ||
               block == Blocks.GREEN_STAINED_GLASS ||
               block == Blocks.RED_STAINED_GLASS ||
               block == Blocks.BLACK_STAINED_GLASS;
    }

    // Проверить является ли блок льдом
    private boolean isIceBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.ICE ||
               block == Blocks.PACKED_ICE ||
               block == Blocks.BLUE_ICE;
    }

    // Метод для разрушения блока и спавна дропа/осколков
    private void breakGlass(Level world, BlockPos pos, BlockState state, Vec3 explosionPos, String blockType, float explosionSize) {
        System.out.println("[ExplosionGlass] === Breaking " + blockType + " block at: " + pos + " ===");

        if (!world.isClientSide) {
            SoundEvent sound = SoundRegistry.random(blockType.equals("glass") ? SoundRegistry.getGlassBreakSounds() : SoundRegistry.getIceBreakSounds());
            if (sound == null) {
                sound = net.minecraft.sounds.SoundEvents.GLASS_BREAK;
            }
            world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        System.out.println("[ExplosionGlass] Block removed from world at: " + pos);

        if (!world.isClientSide) {
            int shardCount;
            if (explosionSize <= 4.0f) {
                shardCount = 5;
            } else if (explosionSize <= 8.0f) {
                shardCount = 4;
            } else if (explosionSize <= 12.0f) {
                shardCount = 3;
            } else {
                shardCount = 2;
            }

            if (state.getBlock() == Blocks.GLASS_PANE || state.getBlock() == Blocks.WHITE_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.ORANGE_STAINED_GLASS_PANE || state.getBlock() == Blocks.MAGENTA_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.LIGHT_BLUE_STAINED_GLASS_PANE || state.getBlock() == Blocks.YELLOW_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.LIME_STAINED_GLASS_PANE || state.getBlock() == Blocks.PINK_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.GRAY_STAINED_GLASS_PANE || state.getBlock() == Blocks.LIGHT_GRAY_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.CYAN_STAINED_GLASS_PANE || state.getBlock() == Blocks.PURPLE_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.BLUE_STAINED_GLASS_PANE || state.getBlock() == Blocks.BROWN_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.GREEN_STAINED_GLASS_PANE || state.getBlock() == Blocks.RED_STAINED_GLASS_PANE
                    || state.getBlock() == Blocks.BLACK_STAINED_GLASS_PANE) {
                shardCount = Math.max(1, shardCount - 1);
            }

            spawnShardEntities(world, pos, state, explosionPos, shardCount);
        }

        if (Config.glassDrops) {
            Block block = state.getBlock();
            ItemStack drop = ItemStack.EMPTY;

            if (blockType.equals("glass")) {
                if (block == Blocks.GLASS || block == Blocks.WHITE_STAINED_GLASS
                        || block == Blocks.GLASS_PANE || block == Blocks.WHITE_STAINED_GLASS_PANE
                        || block == Blocks.ORANGE_STAINED_GLASS || block == Blocks.MAGENTA_STAINED_GLASS
                        || block == Blocks.LIGHT_BLUE_STAINED_GLASS || block == Blocks.YELLOW_STAINED_GLASS
                        || block == Blocks.LIME_STAINED_GLASS || block == Blocks.PINK_STAINED_GLASS
                        || block == Blocks.GRAY_STAINED_GLASS || block == Blocks.LIGHT_GRAY_STAINED_GLASS
                        || block == Blocks.CYAN_STAINED_GLASS || block == Blocks.PURPLE_STAINED_GLASS
                        || block == Blocks.BLUE_STAINED_GLASS || block == Blocks.BROWN_STAINED_GLASS
                        || block == Blocks.GREEN_STAINED_GLASS || block == Blocks.RED_STAINED_GLASS
                        || block == Blocks.BLACK_STAINED_GLASS
                        || block == Blocks.WHITE_STAINED_GLASS_PANE || block == Blocks.ORANGE_STAINED_GLASS_PANE
                        || block == Blocks.MAGENTA_STAINED_GLASS_PANE || block == Blocks.LIGHT_BLUE_STAINED_GLASS_PANE
                        || block == Blocks.YELLOW_STAINED_GLASS_PANE || block == Blocks.LIME_STAINED_GLASS_PANE
                        || block == Blocks.PINK_STAINED_GLASS_PANE || block == Blocks.GRAY_STAINED_GLASS_PANE
                        || block == Blocks.LIGHT_GRAY_STAINED_GLASS_PANE || block == Blocks.CYAN_STAINED_GLASS_PANE
                        || block == Blocks.PURPLE_STAINED_GLASS_PANE || block == Blocks.BLUE_STAINED_GLASS_PANE
                        || block == Blocks.BROWN_STAINED_GLASS_PANE || block == Blocks.GREEN_STAINED_GLASS_PANE
                        || block == Blocks.RED_STAINED_GLASS_PANE || block == Blocks.BLACK_STAINED_GLASS_PANE
                        || block == Blocks.GLASS_PANE) {
                    drop = new ItemStack(block, 1);
                }
            } else if (blockType.equals("ice")) {
                if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
                    drop = new ItemStack(block, 1);
                }
            }

            if (!drop.isEmpty() && Math.random() <= Config.glassDropChance) {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                world.addFreshEntity(itemEntity);
            }
        }
    }

    private void spawnShardEntities(Level world, BlockPos pos, BlockState state, Vec3 origin, int shardCount) {
        ItemStack shardStack = getShardStack(state);
        if (shardStack.isEmpty()) {
            return;
        }

        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        for (int i = 0; i < shardCount; i++) {
            ItemEntity shard = new ItemEntity(world, center.x, center.y, center.z, shardStack.copy());
            shard.setDeltaMovement(randomShardVelocity(origin, center));
            shard.setPickUpDelay(40);
            shard.setXRot((float) (RANDOM.nextFloat() * 360.0f));
            shard.setYRot((float) (RANDOM.nextFloat() * 360.0f));
            setShardLifetime(shard, 60 + RANDOM.nextInt(40));
            world.addFreshEntity(shard);
        }

        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, shardStack),
                    center.x, center.y, center.z,
                    Math.max(6, shardCount * 2),
                    0.2, 0.2, 0.2, 0.02);
        }
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity shard)) {
            return;
        }
        if (shard.level().isClientSide) {
            return;
        }

        ItemStack stack = shard.getItem();
        if (stack.getItem() != ExplosionGlassMod.GLASS_SHARD.get()) {
            return;
        }

        if (!Config.glassShardDamageEnabled) {
            return;
        }

        List<Player> players = shard.level().getEntitiesOfClass(Player.class, shard.getBoundingBox().inflate(0.15), player -> !player.isSpectator() && !player.isCreative());
        if (players.isEmpty()) {
            return;
        }

        CompoundTag tag = shard.getPersistentData();
        long currentTime = shard.level().getGameTime();
        long lastHitTime = tag.contains("LastShardHitTime") ? tag.getLong("LastShardHitTime") : Long.MIN_VALUE;
        if (currentTime - lastHitTime < 10) {
            return;
        }

        tag.putLong("LastShardHitTime", currentTime);
        DamageSource source = shard.level().damageSources().generic();
        for (Player player : players) {
            player.hurt(source, (float) Config.glassShardDamage);
        }
    }

    private ItemStack getShardStack(BlockState state) {
        if (!isGlassBlock(state) && !isIceBlock(state)) {
            return ItemStack.EMPTY;
        }

        ItemStack shardStack = new ItemStack(ExplosionGlassMod.GLASS_SHARD.get());
        int tint = getShardTint(state);
        if (tint != -1) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("ShardColor", tint);
            CustomData.set(DataComponents.CUSTOM_DATA, shardStack, tag);
        }
        return shardStack;
    }

    private int getShardTint(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof StainedGlassBlock stainedGlassBlock) {
            return stainedGlassBlock.getColor().getTextColor();
        }
        if (block instanceof StainedGlassPaneBlock stainedGlassPaneBlock) {
            return stainedGlassPaneBlock.getColor().getTextColor();
        }
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE) {
            return 0xB5E2FF;
        }
        if (block == Blocks.BLUE_ICE) {
            return 0xC0E8FF;
        }
        return 0xFFFFFF;
    }

    private Vec3 randomShardVelocity(Vec3 explosionPos, Vec3 shardPos) {
        Vec3 direction = shardPos.subtract(explosionPos).normalize();
        if (direction.lengthSqr() < 1e-6) {
            direction = new Vec3(0.0, 0.5, 0.0);
        }

        double speed = 0.2 + RANDOM.nextDouble() * 0.25;
        double spread = 0.4;
        return new Vec3(
                direction.x * speed + (RANDOM.nextDouble() - 0.5) * spread,
                Math.max(0.1, direction.y * speed + (RANDOM.nextDouble() - 0.2) * spread),
                direction.z * speed + (RANDOM.nextDouble() - 0.5) * spread);
    }

    private void setShardLifetime(ItemEntity shard, int ticks) {
        String[] candidateNames = new String[] {"lifespan", "life", "age", "timeToDespawn", "despawnTimer"};
        for (String fieldName : candidateNames) {
            try {
                java.lang.reflect.Field field = ItemEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.setInt(shard, ticks);
                return;
            } catch (Exception ignored) {
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

    private float getExplosionSize(Explosion explosion) {
        float explosionSize = 0f;
        try {
            explosionSize = getExplosionFloatField(explosion, "size");
        } catch (Exception ignored) {
            try {
                explosionSize = getExplosionFloatField(explosion, "explosionSize");
            } catch (Exception ignored2) {
                try {
                    java.lang.reflect.Method method = explosion.getClass().getDeclaredMethod("getRadius");
                    method.setAccessible(true);
                    Object result = method.invoke(explosion);
                    if (result instanceof Number) {
                        explosionSize = ((Number) result).floatValue();
                    }
                } catch (Exception ignored3) {
                    try {
                        java.lang.reflect.Method method = explosion.getClass().getDeclaredMethod("getPower");
                        method.setAccessible(true);
                        Object result = method.invoke(explosion);
                        if (result instanceof Number) {
                            explosionSize = ((Number) result).floatValue();
                        }
                    } catch (Exception ignored4) {
                    }
                }
            }
        }
        return explosionSize;
    }

    private float getExplosionFloatField(Explosion explosion, String fieldName) throws Exception {
        java.lang.reflect.Field field = explosion.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(explosion);
        return value instanceof Number ? ((Number) value).floatValue() : 0f;
    }
}
