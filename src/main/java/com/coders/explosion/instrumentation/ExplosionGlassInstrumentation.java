package com.coders.explosion.instrumentation;

import bigworld.InstrumentationAPI;

/**
 * Embedded instrumentation implementation for ExplosionGlass.
 * Registered with the built-in BigWorld integration.
 */
public class ExplosionGlassInstrumentation implements InstrumentationAPI {
    public ExplosionGlassInstrumentation() {
    }

    @Override
    public void instrumentExplosionGlass(Object target) {
        // No special instrumentation required for ExplosionGlass at this time.
        // This method exists to satisfy the embedded BigWorld API.
    }

    @Override
    public String toString() {
        return "ExplosionGlassInstrumentation";
    }
}
