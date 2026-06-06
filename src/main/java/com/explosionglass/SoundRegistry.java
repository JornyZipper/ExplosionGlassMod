package com.explosionglass;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Регистрация всех кастомных звуков для стекла и льда
 */
public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ExplosionGlassMod.MODID);

    // ---- Glass sounds ----
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_BREAK1 = registerSound("glass_break1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_BREAK2 = registerSound("glass_break2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_BREAK3 = registerSound("glass_break3");

    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP1 = registerSound("glass_step1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP2 = registerSound("glass_step2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP3 = registerSound("glass_step3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP4 = registerSound("glass_step4");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP5 = registerSound("glass_step5");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP6 = registerSound("glass_step6");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP7 = registerSound("glass_step7");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP8 = registerSound("glass_step8");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP9 = registerSound("glass_step9");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP10 = registerSound("glass_step10");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_STEP11 = registerSound("glass_step11");

    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_PLACE1 = registerSound("glass_place1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_PLACE2 = registerSound("glass_place2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_PLACE3 = registerSound("glass_place3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_PLACE4 = registerSound("glass_place4");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_PLACE5 = registerSound("glass_place5");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASS_PLACE6 = registerSound("glass_place6");

    // ---- Ice sounds ----
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_BREAK1 = registerSound("ice_break1");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_BREAK2 = registerSound("ice_break2");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_BREAK3 = registerSound("ice_break3");

    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP1 = registerSound("ice_step1");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP2 = registerSound("ice_step2");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP3 = registerSound("ice_step3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP4 = registerSound("ice_step4");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP5 = registerSound("ice_step5");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP6 = registerSound("ice_step6");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP7 = registerSound("ice_step7");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP8 = registerSound("ice_step8");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP9 = registerSound("ice_step9");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP10 = registerSound("ice_step10");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_STEP11 = registerSound("ice_step11");

    // Arrays for random selection using .get() to unwrap DeferredHolder
    public static SoundEvent[] getGlassBreakSounds() {
        return new SoundEvent[]{GLASS_BREAK1.get(), GLASS_BREAK2.get(), GLASS_BREAK3.get()};
    }

    public static SoundEvent[] getGlassStepSounds() {
        return new SoundEvent[]{GLASS_STEP1.get(), GLASS_STEP2.get(), GLASS_STEP3.get(), GLASS_STEP4.get(), GLASS_STEP5.get(), 
                              GLASS_STEP6.get(), GLASS_STEP7.get(), GLASS_STEP8.get(), GLASS_STEP9.get(), GLASS_STEP10.get(), GLASS_STEP11.get()};
    }

    public static SoundEvent[] getGlassPlaceSounds() {
        return new SoundEvent[]{GLASS_PLACE1.get(), GLASS_PLACE2.get(), GLASS_PLACE3.get(), GLASS_PLACE4.get(), GLASS_PLACE5.get(), GLASS_PLACE6.get()};
    }

    public static SoundEvent[] getIceBreakSounds() {
        return new SoundEvent[]{ICE_BREAK1.get(), ICE_BREAK2.get(), ICE_BREAK3.get()};
    }

    public static SoundEvent[] getIceStepSounds() {
        return new SoundEvent[]{ICE_STEP1.get(), ICE_STEP2.get(), ICE_STEP3.get(), ICE_STEP4.get(), ICE_STEP5.get(), 
                              ICE_STEP6.get(), ICE_STEP7.get(), ICE_STEP8.get(), ICE_STEP9.get(), ICE_STEP10.get(), ICE_STEP11.get()};
    }

    // Legacy static arrays for backward compatibility
    public static SoundEvent[] GLASS_BREAK_SOUNDS;
    public static SoundEvent[] GLASS_STEP_SOUNDS;
    public static SoundEvent[] GLASS_PLACE_SOUNDS;
    public static SoundEvent[] ICE_BREAK_SOUNDS;
    public static SoundEvent[] ICE_STEP_SOUNDS;

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ExplosionGlassMod.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    /**
     * Initialize static SoundEvent arrays. Call during common setup when registries are bound.
     */
    public static void initializeArrays() {
        GLASS_BREAK_SOUNDS = getGlassBreakSounds();
        GLASS_STEP_SOUNDS = getGlassStepSounds();
        GLASS_PLACE_SOUNDS = getGlassPlaceSounds();
        ICE_BREAK_SOUNDS = getIceBreakSounds();
        ICE_STEP_SOUNDS = getIceStepSounds();
    }

    // Для выбора случайного звука
    public static SoundEvent random(SoundEvent[] sounds) {
        if (sounds == null || sounds.length == 0) return null;
        return sounds[new Random().nextInt(sounds.length)];
    }
}