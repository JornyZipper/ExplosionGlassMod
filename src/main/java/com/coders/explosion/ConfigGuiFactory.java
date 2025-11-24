package com.coders.explosion;

import com.coders.explosion.ExplosionGlassConfigGui;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ConfigGuiFactory
        implements IModGuiFactory {
    public void initialize(Minecraft minecraftInstance) {
    }

    public boolean hasConfigGui() {
        return true;
    }

    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ExplosionGlassConfigGui(parentScreen);
    }

    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
