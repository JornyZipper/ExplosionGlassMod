package com.coders.explosion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ExplosionGlassConfigGui extends GuiScreen {
    
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("explglass", "textures/gui/general_icons.png");
    
    private GuiScreen parent;
    private GuiButton doneButton;
    private GuiButton cancelButton;
    
    private GuiButton enabledToggle;
    private GuiButton glassDropsToggle;
    private GuiButton losToggle;
    
    private GuiButton blacklistButton;
    private GuiButton whitelistButton;
    
    private GuiTextField radiusField;
    private GuiTextField radiusWithLosField;
    private GuiTextField dropChanceField;
    private GuiTextField losIgnoreField;
    
    public ExplosionGlassConfigGui(GuiScreen parentScreen) {
        this.parent = parentScreen;
    }
    
    @Override
    public void initGui() {
        this.buttonList.clear();
        
        int y = 35;
        int buttonWidth = 200;
        
        // Toggle buttons
        this.enabledToggle = new GuiButton(101, this.width / 2 - 100, y, buttonWidth, 20, 
            I18n.format("config.explosionglass.enabled"));
        this.buttonList.add(enabledToggle);
        y += 25;
        
        this.glassDropsToggle = new GuiButton(102, this.width / 2 - 100, y, buttonWidth, 20,
            I18n.format("config.explosionglass.glassDrops"));
        this.buttonList.add(glassDropsToggle);
        y += 25;
        
        this.losToggle = new GuiButton(103, this.width / 2 - 100, y, buttonWidth, 20,
            I18n.format("config.explosionglass.useLineOfSight"));
        this.buttonList.add(losToggle);
        y += 25;
        
        // Blacklist/Whitelist buttons side by side
        this.blacklistButton = new GuiButton(104, this.width / 2 - 205, y, 95, 20, I18n.format("gui.explosionglass.editblacklist"));
        this.buttonList.add(blacklistButton);
        
        this.whitelistButton = new GuiButton(105, this.width / 2 + 110, y, 95, 20, I18n.format("gui.explosionglass.editwhitelist"));
        this.buttonList.add(whitelistButton);
        y += 30;
        
        // Text field labels and fields
        int labelX = this.width / 2 - 150;
        int fieldX = this.width / 2 + 30;
        
        this.radiusField = new GuiTextField(1, this.fontRenderer, fieldX, y, 60, 18);
        this.radiusField.setText(String.valueOf(ExplosionGlassMod.glassBreakRadius));
        y += 25;
        
        this.radiusWithLosField = new GuiTextField(2, this.fontRenderer, fieldX, y, 60, 18);
        this.radiusWithLosField.setText(String.valueOf(ExplosionGlassMod.glassBreakRadiusWithLoS));
        y += 25;
        
        this.dropChanceField = new GuiTextField(3, this.fontRenderer, fieldX, y, 60, 18);
        this.dropChanceField.setText(String.valueOf(ExplosionGlassMod.glassDropChance));
        y += 25;
        
        this.losIgnoreField = new GuiTextField(4, this.fontRenderer, fieldX, y, 60, 18);
        this.losIgnoreField.setText(String.valueOf(ExplosionGlassMod.loSIgnoreDistance));
        
        // Bottom buttons
        this.doneButton = new GuiButton(200, this.width / 2 - 105, this.height - 30, 100, 20, I18n.format("gui.explosionglass.done"));
        this.cancelButton = new GuiButton(201, this.width / 2 + 5, this.height - 30, 100, 20, I18n.format("gui.explosionglass.cancel"));
        this.buttonList.add(doneButton);
        this.buttonList.add(cancelButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 101) {
            ExplosionGlassMod.Mod = !ExplosionGlassMod.Mod;
        } else if (button.id == 102) {
            ExplosionGlassMod.glassDrops = !ExplosionGlassMod.glassDrops;
        } else if (button.id == 103) {
            ExplosionGlassMod.useLineOfSight = !ExplosionGlassMod.useLineOfSight;
        } else if (button.id == 104) {
            // Open blacklist editor
            this.mc.displayGuiScreen(new ListEditorGui(this, "Blacklist", ExplosionGlassMod.BLACKLIST, true));
        } else if (button.id == 105) {
            // Open whitelist editor
            this.mc.displayGuiScreen(new ListEditorGui(this, "Whitelist", ExplosionGlassMod.WHITELIST, false));
        } else if (button.id == 200) {
            // Save
            try {
                ExplosionGlassMod.glassBreakRadius = Integer.parseInt(radiusField.getText());
                ExplosionGlassMod.glassBreakRadiusWithLoS = Integer.parseInt(radiusWithLosField.getText());
                ExplosionGlassMod.glassDropChance = Double.parseDouble(dropChanceField.getText());
                ExplosionGlassMod.loSIgnoreDistance = Double.parseDouble(losIgnoreField.getText());
                
                if (ExplosionGlassMod.config != null) {
                    ExplosionGlassMod.config.save();
                    System.out.println("[ExplosionGlass] Config saved!");
                }
            } catch (NumberFormatException e) {
                System.err.println("[ExplosionGlass] Invalid number: " + e.getMessage());
            }
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 201) {
            // Cancel
            this.mc.displayGuiScreen(parent);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        this.drawCenteredString(this.fontRenderer, "ExplosionGlass Config", this.width / 2, 15, 0xFFFFFF);
        
        // Draw toggle indicators (checkmarks and X's)
        int indicatorX = this.width / 2 + 115;
        
        // Enabled toggle indicator
        String enabledStatus = ExplosionGlassMod.Mod ? "✓" : "✗";
        int enabledColor = ExplosionGlassMod.Mod ? 0x00FF00 : 0xFF0000;  // Green for ON, Red for OFF
        this.drawString(this.fontRenderer, enabledStatus, indicatorX, 39, enabledColor);
        
        // Glass Drops toggle indicator
        String dropsStatus = ExplosionGlassMod.glassDrops ? "✓" : "✗";
        int dropsColor = ExplosionGlassMod.glassDrops ? 0x00FF00 : 0xFF0000;
        this.drawString(this.fontRenderer, dropsStatus, indicatorX, 64, dropsColor);
        
        // Line of Sight toggle indicator
        String losStatus = ExplosionGlassMod.useLineOfSight ? "✓" : "✗";
        int losColor = ExplosionGlassMod.useLineOfSight ? 0x00FF00 : 0xFF0000;
        this.drawString(this.fontRenderer, losStatus, indicatorX, 89, losColor);
        
        // Draw labels on the left
        int labelX = this.width / 2 - 150;
        int labelY = 145;
        this.drawString(this.fontRenderer, I18n.format("config.explosionglass.glassBreakRadius") + ":", labelX, labelY, 0xAAAAAA);
        this.drawString(this.fontRenderer, I18n.format("config.explosionglass.glassBreakRadiusWithLoS") + ":", labelX, labelY + 25, 0xAAAAAA);
        this.drawString(this.fontRenderer, I18n.format("config.explosionglass.glassDropChance") + ":", labelX, labelY + 50, 0xAAAAAA);
        this.drawString(this.fontRenderer, I18n.format("config.explosionglass.loSIgnoreDistance") + ":", labelX, labelY + 75, 0xAAAAAA);
        
        // Draw text fields
        radiusField.drawTextBox();
        radiusWithLosField.drawTextBox();
        dropChanceField.drawTextBox();
        losIgnoreField.drawTextBox();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawToggleIcon(int x, int y, boolean enabled) {
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(x, y, enabled ? 0 : 16, 0, 16, 16);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        radiusField.textboxKeyTyped(typedChar, keyCode);
        radiusWithLosField.textboxKeyTyped(typedChar, keyCode);
        dropChanceField.textboxKeyTyped(typedChar, keyCode);
        losIgnoreField.textboxKeyTyped(typedChar, keyCode);
        
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(parent);
        }
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        radiusField.mouseClicked(mouseX, mouseY, mouseButton);
        radiusWithLosField.mouseClicked(mouseX, mouseY, mouseButton);
        dropChanceField.mouseClicked(mouseX, mouseY, mouseButton);
        losIgnoreField.mouseClicked(mouseX, mouseY, mouseButton);
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void onGuiClosed() {
        if (ExplosionGlassMod.config != null) {
            ExplosionGlassMod.config.save();
        }
    }
}
