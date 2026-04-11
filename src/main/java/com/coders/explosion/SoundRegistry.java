package com.coders.explosion;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

/**
 * Регистрация всех кастомных звуков для стекла и льда
 */
@Mod.EventBusSubscriber(modid = "explglass")
public class SoundRegistry {
    private static final Random rand = new Random();

    // ---- Glass sounds ----
    public static SoundEvent[] GLASS_BREAK_SOUNDS;
    public static SoundEvent[] GLASS_STEP_SOUNDS;
    public static SoundEvent[] GLASS_PLACE_SOUNDS;

    // ---- Ice sounds ----
    public static SoundEvent[] ICE_BREAK_SOUNDS;
    public static SoundEvent[] ICE_STEP_SOUNDS;

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();

        // Glass
        GLASS_BREAK_SOUNDS = new SoundEvent[] {
                registerSound(registry, "explglass", "glass_break1"),
                registerSound(registry, "explglass", "glass_break2"),
                registerSound(registry, "explglass", "glass_break3")
        };
        GLASS_STEP_SOUNDS = new SoundEvent[] {
                registerSound(registry, "explglass", "glass_step1"),
                registerSound(registry, "explglass", "glass_step2"),
                registerSound(registry, "explglass", "glass_step3"),
                registerSound(registry, "explglass", "glass_step4"),
                registerSound(registry, "explglass", "glass_step5"),
                registerSound(registry, "explglass", "glass_step6"),
                registerSound(registry, "explglass", "glass_step7"),
                registerSound(registry, "explglass", "glass_step8"),
                registerSound(registry, "explglass", "glass_step9"),
                registerSound(registry, "explglass", "glass_step10"),
                registerSound(registry, "explglass", "glass_step11")
        };
        GLASS_PLACE_SOUNDS = new SoundEvent[] {
                registerSound(registry, "explglass", "glass_place1"),
                registerSound(registry, "explglass", "glass_place2"),
                registerSound(registry, "explglass", "glass_place3"),
                registerSound(registry, "explglass", "glass_place4"),
                registerSound(registry, "explglass", "glass_place5"),
                registerSound(registry, "explglass", "glass_place6")
        };

        // Ice
        ICE_BREAK_SOUNDS = new SoundEvent[] {
                registerSound(registry, "explglass", "ice_break1"),
                registerSound(registry, "explglass", "ice_break2"),
                registerSound(registry, "explglass", "ice_break3")
        };
        ICE_STEP_SOUNDS = new SoundEvent[] {
                registerSound(registry, "explglass", "ice_step1"),
                registerSound(registry, "explglass", "ice_step2"),
                registerSound(registry, "explglass", "ice_step3"),
                registerSound(registry, "explglass", "ice_step4"),
                registerSound(registry, "explglass", "ice_step5"),
                registerSound(registry, "explglass", "ice_step6"),
                registerSound(registry, "explglass", "ice_step7"),
                registerSound(registry, "explglass", "ice_step8"),
                registerSound(registry, "explglass", "ice_step9"),
                registerSound(registry, "explglass", "ice_step10"),
                registerSound(registry, "explglass", "ice_step11")
        };

        // После регистрации звуков, сразу настраиваем кастомные SoundType для блоков
        // GlassSoundBlocker.setupCustomGlassSounds(); // Moved to init
    }

    private static SoundEvent registerSound(IForgeRegistry<SoundEvent> registry, String modId, String soundName) {
        ResourceLocation location = new ResourceLocation(modId, soundName);
        SoundEvent event = new SoundEvent(location).setRegistryName(location);
        registry.register(event);
        System.out.println("[ExplosionGlass] Registered sound: " + location);
        return event;
    }

    // Для выбора случайного звука
    public static SoundEvent random(SoundEvent[] sounds) {
        if (sounds == null || sounds.length == 0) return null;
        return sounds[new Random().nextInt(sounds.length)];
    }
}