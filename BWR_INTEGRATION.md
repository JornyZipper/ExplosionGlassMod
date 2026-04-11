BWR-Core integration
=====================

This mod uses BWR-Core as the authoritative Line-Of-Sight (LOS) provider.

Summary:
- The internal LOS implementation has been removed; LOS checks are delegated to BWR-Core's ILOS provider via reflection (`BwrLosBridge`).
- The mod declares a runtime dependency on the `bwr_core` mod (`required-after:bwr_core` in `@Mod`).
- If BWR-Core is not present, LOS checks return `false` (no LoS), and the mod will not perform internal LOS computations.

Usage example (mod decides when to call LOS):

```java
if (Loader.isModLoaded("bwr_core")) {
    boolean visible = com.coders.explosion.bwr.BwrLosBridge.canSee(world, fromVec, targetPos);
    if (visible) {
        // handle logic
    }
}
```

Performance:
- `BwrLosBridge` uses reflection to invoke BWR-Core APIs. The ideal integration is to compile against `bwr-core` and call the `ILOSProvider` directly.
- BWR-Core implementations should be optimized (3D Bresenham, internal caching).
