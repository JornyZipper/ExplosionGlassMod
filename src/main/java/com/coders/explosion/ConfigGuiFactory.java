package com.coders.explosion;

import com.coders.explosion.ExplosionGlassConfigGui;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ConfigGuiFactory implements IModGuiFactory {
    
    @Override
    public void initialize(Minecraft minecraftInstance) {
        System.out.println("[ExplosionGlass] ConfigGuiFactory.initialize() called");
    }

    @Override
    public boolean hasConfigGui() {
        System.out.println("[ExplosionGlass] ConfigGuiFactory.hasConfigGui() called");
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        System.out.println("[ExplosionGlass] ConfigGuiFactory.createConfigGui() called with parent: " + parentScreen);
        try {
            GuiScreen gui = new ExplosionGlassConfigGui(parentScreen);
            System.out.println("[ExplosionGlass] ConfigGui created successfully: " + gui);
            return gui;
        } catch (Exception e) {
            System.err.println("[ExplosionGlass] ERROR creating config GUI: " + e.getMessage());
            e.printStackTrace();
            final GuiScreen parent = parentScreen;
            return new GuiScreen() {
                @Override
                public void initGui() {
                    this.buttonList.clear();
                    this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 120, 200, 20, "Close (config failed)"));
                }

                @Override
                protected void actionPerformed(GuiButton button) {
                    if (button.id == 200) {
                        Minecraft.getMinecraft().displayGuiScreen(parent);
                    }
                }

                @Override
                public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                    this.drawDefaultBackground();
                    this.drawCenteredString(this.fontRenderer, "ExplosionGlass: failed to open config", this.width / 2, this.height / 2 - 20, 0xFFFFFF);
                    super.drawScreen(mouseX, mouseY, partialTicks);
                }
            };
        }
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
