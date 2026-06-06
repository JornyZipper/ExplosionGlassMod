package bigworld.shard;

import net.minecraft.world.World;

public class ShardAPI {

    /**
     * Spawn shard explosion.
     */
    public static void spawnShards(World world,
                                   double x,
                                   double y,
                                   double z,
                                   int count,
                                   double power) {

        if (world.isRemote) return;

        for (int i = 0; i < count; i++) {

            ShardEntity shard = new ShardEntity(world);

            shard.setPosition(x, y, z);

            shard.motionX = (world.rand.nextDouble() - 0.5) * power;
            shard.motionY = world.rand.nextDouble() * power;
            shard.motionZ = (world.rand.nextDouble() - 0.5) * power;

            world.spawnEntity(shard);
        }
    }

    /**
     * Spawn 10 glass shard entities with randomized velocity.
     * Handles all shard physics and damage logic inside BigWorld.
     * 
     * @param world The world to spawn shards in
     * @param pos   The block position to spawn shards from
     */
    public static void spawnGlassShards(World world, net.minecraft.util.math.BlockPos pos) {
        spawnShards(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.8);
    }

    /**
     * Вычислить траекторию осколка и проверить столкновения с использованием LOS.
     * Возвращает true, если путь свободен, false если заблокирован.
     * 
     * Пример использования:
     * boolean canHit = ShardAPI.computeShardTrajectoryAndCheckLOS(world, 
     *     shard.posX, shard.posY, shard.posZ, 
     *     target.posX, target.posY, target.posZ, true);
     */
    public static boolean computeShardTrajectoryAndCheckLOS(World world,
                                                             double startX, double startY, double startZ,
                                                             double endX, double endY, double endZ,
                                                             boolean ignoreTransparent) {
        // Используем LOSManager для проверки видимости между начальной и конечной точкой
        bigworld.los.LOSManager losManager = new bigworld.los.LOSManager(true); // с кэшем

        net.minecraft.util.math.BlockPos startPos = new net.minecraft.util.math.BlockPos(startX, startY, startZ);
        net.minecraft.util.math.BlockPos endPos = new net.minecraft.util.math.BlockPos(endX, endY, endZ);

        return losManager.hasLineOfSight(world, startPos, endPos, ignoreTransparent);
    }

    /**
     * Вычислить примерную траекторию осколка с учетом гравитации и времени.
     * Возвращает конечную позицию после ticks тиков.
     * 
     * Пример использования:
     * double[] finalPos = ShardAPI.computeShardTrajectory(
     *     shard.posX, shard.posY, shard.posZ, 
     *     shard.motionX, shard.motionY, shard.motionZ, 100);
     * System.out.println("Осколок будет в: " + finalPos[0] + ", " + finalPos[1] + ", " + finalPos[2]);
     */
    public static double[] computeShardTrajectory(double startX, double startY, double startZ,
                                                  double motionX, double motionY, double motionZ,
                                                  int ticks) {
        double gravity = 0.03D; // гравитация из ShardEntity
        double damping = 0.98D; // затухание

        double x = startX;
        double y = startY;
        double z = startZ;
        double vx = motionX;
        double vy = motionY;
        double vz = motionZ;

        for (int t = 0; t < ticks; t++) {
            // применить гравитацию
            vy -= gravity;

            // переместить
            x += vx;
            y += vy;
            z += vz;

            // применить затухание
            vx *= damping;
            vy *= damping;
            vz *= damping;
        }

        return new double[]{x, y, z};
    }

}