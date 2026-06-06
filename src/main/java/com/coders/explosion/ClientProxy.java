package com.coders.explosion;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.SoundEvent;
import net.minecraft.init.Blocks;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        // No entities to render
        MinecraftForge.EVENT_BUS.register(new UpdateNoticeHandler());
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() == null || event.getSound().getSoundLocation() == null) return;

        String soundName = event.getSound().getSoundLocation().toString();
        World world = Minecraft.getMinecraft().world;
        if (world == null) return;

        BlockPos pos = new BlockPos(event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF());

        // First, check what block is at this position
        boolean isGlass = isGlassBlock(world, pos);
        boolean isIce = isIceBlock(world, pos);

        // If it's glass, only handle glass sounds
        if (isGlass) {
            if (soundName.contains("block.glass") || soundName.contains("block.stained_glass") ||
                soundName.equals("minecraft:block.glass.break") ||
                soundName.equals("minecraft:block.glass.place") ||
                soundName.equals("minecraft:block.glass.step") ||
                soundName.equals("minecraft:block.glass.hit") ||
                soundName.equals("minecraft:block.glass.fall")) {
                
                System.out.println("[ExplosionGlass] Blocking vanilla glass sound: " + soundName);
                event.setResultSound(null);
                playCustomGlassSound(world, pos, soundName);
            }
        }
        // If it's ice, only handle ice sounds - NOT glass sounds
        else if (isIce) {
            if (soundName.contains("block.snow") || soundName.contains("block.ice") ||
                soundName.equals("minecraft:block.snow.break") ||
                soundName.equals("minecraft:block.snow.place") ||
                soundName.equals("minecraft:block.snow.step") ||
                soundName.equals("minecraft:block.snow.hit") ||
                soundName.equals("minecraft:block.snow.fall")) {
                
                System.out.println("[ExplosionGlass] Blocking vanilla ice sound: " + soundName);
                event.setResultSound(null);
                playCustomIceSound(world, pos, soundName);
            }
        }
    }

    private boolean isGlassBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.GLASS ||
               world.getBlockState(pos).getBlock() == Blocks.STAINED_GLASS ||
               world.getBlockState(pos).getBlock() == Blocks.GLASS_PANE ||
               world.getBlockState(pos).getBlock() == Blocks.STAINED_GLASS_PANE;
    }

    private boolean isIceBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.ICE ||
               world.getBlockState(pos).getBlock() == Blocks.PACKED_ICE ||
               world.getBlockState(pos).getBlock() == Blocks.FROSTED_ICE;
    }

    private void playCustomGlassSound(World world, BlockPos pos, String soundType) {
        if (SoundRegistry.GLASS_BREAK_SOUNDS == null) return;

        SoundEvent sound = null;
        if (soundType.contains("break")) {
            sound = SoundRegistry.random(SoundRegistry.GLASS_BREAK_SOUNDS);
        } else if (soundType.contains("place")) {
            sound = SoundRegistry.random(SoundRegistry.GLASS_PLACE_SOUNDS);
        } else if (soundType.contains("step")) {
            sound = SoundRegistry.random(SoundRegistry.GLASS_STEP_SOUNDS);
        }

        if (sound != null) {
            System.out.println("[ExplosionGlass] Playing custom glass sound: " + sound.getSoundName());
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    private void playCustomIceSound(World world, BlockPos pos, String soundType) {
        if (SoundRegistry.ICE_BREAK_SOUNDS == null) return;

        SoundEvent sound = null;
        if (soundType.contains("break")) {
            sound = SoundRegistry.random(SoundRegistry.ICE_BREAK_SOUNDS);
        } else if (soundType.contains("step")) {
            sound = SoundRegistry.random(SoundRegistry.ICE_STEP_SOUNDS);
        }

        if (sound != null) {
            System.out.println("[ExplosionGlass] Playing custom ice sound: " + sound.getSoundName());
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
    }
}