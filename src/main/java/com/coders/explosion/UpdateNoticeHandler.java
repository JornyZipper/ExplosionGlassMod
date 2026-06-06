package com.coders.explosion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UpdateNoticeHandler {
    private boolean shown = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shown) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        if (!(mc.currentScreen instanceof GuiMainMenu)) return;

        boolean bwrDetected = Loader.isModLoaded("bigworld");
        if (bwrDetected || ExplosionGlassMod.showUpdateNotice) {
            shown = true;
            mc.displayGuiScreen(new UpdateNoticeGui(bwrDetected));
        }
    }
}
