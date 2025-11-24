package com.coders.explosion;

import com.coders.explosion.ExplosionGlassMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ExplosionGlassConfigGui
        extends GuiConfig {
    public ExplosionGlassConfigGui(GuiScreen parent) {
        super(parent, new ConfigElement(ExplosionGlassMod.config.getCategory("general")).getChildElements(), "explosionglass", false, false, "ExplosionGlass Config");
    }
}
