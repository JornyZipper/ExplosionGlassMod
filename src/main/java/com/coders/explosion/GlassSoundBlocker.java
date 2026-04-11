package com.coders.explosion;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundEvent;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Настройка кастомных SoundType для стекла и льда
 */
public class GlassSoundBlocker {

    private static final Random rand = new Random();

    public static class CustomSoundType extends SoundType {
        private SoundEvent[] breakSounds;
        private SoundEvent[] stepSounds;
        private SoundEvent[] placeSounds;

        public CustomSoundType(float volume, float pitch, SoundEvent[] breakSounds, SoundEvent[] stepSounds, SoundEvent[] placeSounds) {
            super(volume, pitch, breakSounds[0], stepSounds[0], placeSounds != null ? placeSounds[0] : null, null, null);
            this.breakSounds = breakSounds;
            this.stepSounds = stepSounds;
            this.placeSounds = placeSounds;
        }

        @Override
        public SoundEvent getBreakSound() {
            SoundEvent sound = random(breakSounds);
            System.out.println("[ExplosionGlass] getBreakSound called, returning: " + sound);
            return sound;
        }

        @Override
        public SoundEvent getStepSound() {
            SoundEvent sound = random(stepSounds);
            System.out.println("[ExplosionGlass] getStepSound called, returning: " + sound);
            return sound;
        }

        @Override
        public SoundEvent getPlaceSound() {
            SoundEvent sound = placeSounds != null ? random(placeSounds) : super.getPlaceSound();
            System.out.println("[ExplosionGlass] getPlaceSound called, returning: " + sound);
            return sound;
        }

        private SoundEvent random(SoundEvent[] sounds) {
            if (sounds == null || sounds.length == 0) return null;
            return sounds[rand.nextInt(sounds.length)];
        }
    }

    public static void setupCustomGlassSounds() {
        try {
            if (SoundRegistry.GLASS_BREAK_SOUNDS == null || SoundRegistry.GLASS_STEP_SOUNDS == null ||
                SoundRegistry.GLASS_PLACE_SOUNDS == null || SoundRegistry.ICE_BREAK_SOUNDS == null ||
                SoundRegistry.ICE_STEP_SOUNDS == null) {
                    
                System.out.println("[ExplosionGlass] ERROR: Sound events not registered yet!");
                return;
            }

            Field soundTypeField = null;
            String[] possibleFieldNames = {"field_149762_H", "blockSoundType", "soundType"};

            for (String name : possibleFieldNames) {
                try {
                    soundTypeField = Block.class.getDeclaredField(name);
                    break;
                } catch (NoSuchFieldException ignored) {}
            }

            if (soundTypeField == null) {
                System.out.println("[ExplosionGlass] WARNING: Could not find soundType field!");
                return;
            }
            soundTypeField.setAccessible(true);
            System.out.println("[ExplosionGlass] Using soundType field: " + soundTypeField.getName());

            // ---- Glass SoundType ----
            SoundType glassSoundType = new CustomSoundType(1.0F, 1.0F,
                    SoundRegistry.GLASS_BREAK_SOUNDS,
                    SoundRegistry.GLASS_STEP_SOUNDS,
                    SoundRegistry.GLASS_PLACE_SOUNDS
            );

            // ---- Ice SoundType ----
            // Для льда используем step звуки как place звуки, так как специальных place звуков нет
            SoundType iceSoundType = new CustomSoundType(1.0F, 1.0F,
                    SoundRegistry.ICE_BREAK_SOUNDS,
                    SoundRegistry.ICE_STEP_SOUNDS,
                    SoundRegistry.ICE_STEP_SOUNDS  // Use step sounds for place
            );

            // Применяем к стеклу
            soundTypeField.set(Blocks.GLASS, glassSoundType);
            soundTypeField.set(Blocks.STAINED_GLASS, glassSoundType);
            soundTypeField.set(Blocks.GLASS_PANE, glassSoundType);
            soundTypeField.set(Blocks.STAINED_GLASS_PANE, glassSoundType);

            // Применяем к льду
            soundTypeField.set(Blocks.ICE, iceSoundType);
            soundTypeField.set(Blocks.PACKED_ICE, iceSoundType);
            soundTypeField.set(Blocks.FROSTED_ICE, iceSoundType);

            System.out.println("[ExplosionGlass] Glass & ice sounds applied successfully! Glass sound: " + Blocks.GLASS.getSoundType() + ", Ice sound: " + Blocks.ICE.getSoundType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}