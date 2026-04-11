package com.coders.explosion.bwr;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.junit.Test;

import static org.junit.Assert.*;

public class BwrLosBridgeTest {

    @Test
    public void bridgeReturnsBooleanWhenProviderAbsent() {
        // Without BWR-Core present, canSee should return false safely
        boolean result = BwrLosBridge.canSee(null, new Vec3d(0,0,0), new BlockPos(0,0,0));
        assertFalse(result);
    }

}
