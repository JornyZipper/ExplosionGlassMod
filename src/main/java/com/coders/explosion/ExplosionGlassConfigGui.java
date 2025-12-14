package com.coders.explosion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

public class ExplosionGlassConfigGui extends GuiScreen {
    
    private GuiScreen parent;
    private GuiButton doneButton;
    private GuiButton cancelButton;
    private GuiButton resetButton;
    
    private GuiButton enabledToggle;
    private GuiButton glassDropsToggle;
    private GuiButton losToggle;
    private GuiButton blacklistButton;
    private GuiButton whitelistButton;
    
    private GuiTextField radiusField;
    private GuiTextField radiusWithLosField;
    private GuiTextField dropChanceField;
    private GuiTextField losIgnoreField;
    
    private int scrollOffset = 0;
    private static final int SCROLL_STEP = 30;
    
    public ExplosionGlassConfigGui(GuiScreen parentScreen) {
        this.parent = parentScreen;
    }
    
    @Override
    public void initGui() {
        this.buttonList.clear();
        
        // Toggle buttons for boolean settings - WIDER
        this.enabledToggle = new GuiButton(101, 50, 50 - scrollOffset, 300, 20, 
            net.minecraft.client.resources.I18n.format("config.explosionglass.enabled") + ": " + (ExplosionGlassMod.Mod ? "ON" : "OFF"));
        this.glassDropsToggle = new GuiButton(102, 50, 75 - scrollOffset, 300, 20, 
            net.minecraft.client.resources.I18n.format("config.explosionglass.glassDrops") + ": " + (ExplosionGlassMod.glassDrops ? "ON" : "OFF"));
        this.losToggle = new GuiButton(103, 50, 100 - scrollOffset, 300, 20, 
            net.minecraft.client.resources.I18n.format("config.explosionglass.useLineOfSight") + ": " + (ExplosionGlassMod.useLineOfSight ? "ON" : "OFF"));
        this.blacklistButton = new GuiButton(104, 50, 125 - scrollOffset, 145, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.editblacklist"));
        this.whitelistButton = new GuiButton(105, 205, 125 - scrollOffset, 145, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.editwhitelist"));
        
        if (enabledToggle.y > 40 && enabledToggle.y < this.height - 50) this.buttonList.add(enabledToggle);
        if (glassDropsToggle.y > 40 && glassDropsToggle.y < this.height - 50) this.buttonList.add(glassDropsToggle);
        if (losToggle.y > 40 && losToggle.y < this.height - 50) this.buttonList.add(losToggle);
        if (blacklistButton.y > 40 && blacklistButton.y < this.height - 50) this.buttonList.add(blacklistButton);
        if (whitelistButton.y > 40 && whitelistButton.y < this.height - 50) this.buttonList.add(whitelistButton);
        
        // Create text fields for numeric settings - ONLY 2 RADIUS FIELDS
        this.radiusField = new GuiTextField(1, this.fontRenderer, 50, 160 - scrollOffset, 140, 20);
        this.radiusField.setText(String.valueOf(ExplosionGlassMod.glassBreakRadius));
        
        this.radiusWithLosField = new GuiTextField(2, this.fontRenderer, 210, 160 - scrollOffset, 140, 20);
        this.radiusWithLosField.setText(String.valueOf(ExplosionGlassMod.glassBreakRadiusWithLoS));
        
        this.dropChanceField = new GuiTextField(3, this.fontRenderer, 50, 210 - scrollOffset, 140, 20);
        this.dropChanceField.setText(String.valueOf(ExplosionGlassMod.glassDropChance));
        
        this.losIgnoreField = new GuiTextField(4, this.fontRenderer, 210, 210 - scrollOffset, 140, 20);
        this.losIgnoreField.setText(String.valueOf(ExplosionGlassMod.loSIgnoreDistance));
        
        // Add Done/Cancel buttons
        this.doneButton = new GuiButton(200, 50, this.height - 30, 145, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.done"));
        this.cancelButton = new GuiButton(201, 205, this.height - 30, 145, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.cancel"));
        // Place reset button a bit closer to the right edge (smaller margin)
        this.resetButton = new GuiButton(202, this.width - 10 - 100, this.height - 30, 100, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.reset"));
        this.buttonList.add(doneButton);
        this.buttonList.add(cancelButton);
        this.buttonList.add(resetButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 101) {
            ExplosionGlassMod.Mod = !ExplosionGlassMod.Mod;
            enabledToggle.displayString = net.minecraft.client.resources.I18n.format("config.explosionglass.enabled") + ": " + (ExplosionGlassMod.Mod ? "ON" : "OFF");
        } else if (button.id == 102) {
            ExplosionGlassMod.glassDrops = !ExplosionGlassMod.glassDrops;
            glassDropsToggle.displayString = net.minecraft.client.resources.I18n.format("config.explosionglass.glassDrops") + ": " + (ExplosionGlassMod.glassDrops ? "ON" : "OFF");
        } else if (button.id == 103) {
            ExplosionGlassMod.useLineOfSight = !ExplosionGlassMod.useLineOfSight;
            losToggle.displayString = net.minecraft.client.resources.I18n.format("config.explosionglass.useLineOfSight") + ": " + (ExplosionGlassMod.useLineOfSight ? "ON" : "OFF");
        } else if (button.id == 104) {
            // Open blacklist editor
            this.mc.displayGuiScreen(new ListEditorGui(this, "Blacklist", ExplosionGlassMod.glassBlacklist, true));
        } else if (button.id == 105) {
            // Open whitelist editor
            this.mc.displayGuiScreen(new ListEditorGui(this, "Whitelist", ExplosionGlassMod.glassWhitelist, false));
        } else if (button.id == 200) {
            // Save and close
            try {
                ExplosionGlassMod.glassBreakRadius = Integer.parseInt(radiusField.getText());
                ExplosionGlassMod.glassBreakRadiusWithLoS = Integer.parseInt(radiusWithLosField.getText());
                ExplosionGlassMod.glassDropChance = Double.parseDouble(dropChanceField.getText());
                ExplosionGlassMod.loSIgnoreDistance = Double.parseDouble(losIgnoreField.getText());
                
                if (ExplosionGlassMod.config != null) {
                    ExplosionGlassMod.config.save();
                }
            } catch (NumberFormatException e) {
                System.err.println("[ExplosionGlass] Invalid number format: " + e.getMessage());
            }
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 201) {
            // Cancel - discard changes
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 202) {
            // Reset to defaults
            ExplosionGlassMod.Mod = true;
            ExplosionGlassMod.glassBreakRadius = 20;
            ExplosionGlassMod.glassBreakRadiusWithLoS = 10;
            ExplosionGlassMod.glassBlacklist = new String[0];
            ExplosionGlassMod.glassWhitelist = new String[0];
            ExplosionGlassMod.useLineOfSight = true;
            ExplosionGlassMod.glassDrops = false;
            ExplosionGlassMod.glassDropChance = 1.0;
            ExplosionGlassMod.loSIgnoreDistance = 10.0;

            // Update GUI controls
            enabledToggle.displayString = net.minecraft.client.resources.I18n.format("config.explosionglass.enabled") + ": " + (ExplosionGlassMod.Mod ? "ON" : "OFF");
            glassDropsToggle.displayString = net.minecraft.client.resources.I18n.format("config.explosionglass.glassDrops") + ": " + (ExplosionGlassMod.glassDrops ? "ON" : "OFF");
            losToggle.displayString = net.minecraft.client.resources.I18n.format("config.explosionglass.useLineOfSight") + ": " + (ExplosionGlassMod.useLineOfSight ? "ON" : "OFF");

            radiusField.setText(String.valueOf(ExplosionGlassMod.glassBreakRadius));
            radiusWithLosField.setText(String.valueOf(ExplosionGlassMod.glassBreakRadiusWithLoS));
            dropChanceField.setText(String.valueOf(ExplosionGlassMod.glassDropChance));
            losIgnoreField.setText(String.valueOf(ExplosionGlassMod.loSIgnoreDistance));

            // Persist defaults to config
            if (ExplosionGlassMod.config != null) {
                ExplosionGlassMod.config.get("general", "enabled", ExplosionGlassMod.Mod).set(String.valueOf(ExplosionGlassMod.Mod));
                ExplosionGlassMod.config.get("general", "glassBreakRadius", ExplosionGlassMod.glassBreakRadius).set(String.valueOf(ExplosionGlassMod.glassBreakRadius));
                ExplosionGlassMod.config.get("general", "glassBreakRadiusWithLoS", ExplosionGlassMod.glassBreakRadiusWithLoS).set(String.valueOf(ExplosionGlassMod.glassBreakRadiusWithLoS));
                ExplosionGlassMod.config.get("general", "glassBlacklist", ExplosionGlassMod.glassBlacklist).set(ExplosionGlassMod.glassBlacklist);
                ExplosionGlassMod.config.get("general", "glassWhitelist", ExplosionGlassMod.glassWhitelist).set(ExplosionGlassMod.glassWhitelist);
                ExplosionGlassMod.config.get("general", "useLineOfSight", ExplosionGlassMod.useLineOfSight).set(String.valueOf(ExplosionGlassMod.useLineOfSight));
                ExplosionGlassMod.config.get("general", "glassDrops", ExplosionGlassMod.glassDrops).set(String.valueOf(ExplosionGlassMod.glassDrops));
                ExplosionGlassMod.config.get("general", "glassDropChance", ExplosionGlassMod.glassDropChance).set(String.valueOf(ExplosionGlassMod.glassDropChance));
                ExplosionGlassMod.config.get("general", "loSIgnoreDistance", ExplosionGlassMod.loSIgnoreDistance).set(String.valueOf(ExplosionGlassMod.loSIgnoreDistance));
                ExplosionGlassMod.config.save();
            }
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "ExplosionGlass " + net.minecraft.client.resources.I18n.format("config.explosionglass.enabled"), this.width / 2, 20, 0xFFFFFF);
        this.drawString(this.fontRenderer, net.minecraft.client.resources.I18n.format("gui.explosionglass.scroll"), 30, this.height - 50, 0xAAAAAA);
        
        // Draw labels for numeric fields with adjusted Y positions - ONLY 2 FIELDS
        drawWrappedText(50, 145 - scrollOffset, 130, net.minecraft.client.resources.I18n.format("config.explosionglass.glassBreakRadius") + ":", 0xFFFFFF);
        drawWrappedText(210, 145 - scrollOffset, 130, net.minecraft.client.resources.I18n.format("config.explosionglass.glassBreakRadiusWithLoS") + ":", 0xFFFFFF);
        
        // Draw hidden labels for other fields
        drawWrappedText(50, 195 - scrollOffset, 130, net.minecraft.client.resources.I18n.format("config.explosionglass.glassDropChance") + ":", 0xAAAAAA);
        drawWrappedText(210, 195 - scrollOffset, 130, net.minecraft.client.resources.I18n.format("config.explosionglass.loSIgnoreDistance") + ":", 0xAAAAAA);
        
        // Draw text fields - ONLY 2 VISIBLE
        if (radiusField.y > 40 && radiusField.y < this.height - 50) radiusField.drawTextBox();
        if (radiusWithLosField.y > 40 && radiusWithLosField.y < this.height - 50) radiusWithLosField.drawTextBox();
        
        // Draw other fields in grey/hidden area
        if (dropChanceField.y > 40 && dropChanceField.y < this.height - 50) dropChanceField.drawTextBox();
        if (losIgnoreField.y > 40 && losIgnoreField.y < this.height - 50) losIgnoreField.drawTextBox();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    // Метод для рисования текста с переносом строк
    private void drawWrappedText(int x, int y, int maxWidth, String text, int color) {
        String[] words = text.split(" ");
        String line = "";
        int currentY = y;
        
        for (String word : words) {
            String testLine = line.isEmpty() ? word : line + " " + word;
            if (this.fontRenderer.getStringWidth(testLine) <= maxWidth) {
                line = testLine;
            } else {
                if (!line.isEmpty()) {
                    this.drawString(this.fontRenderer, line, x, currentY, color);
                    currentY += 10;
                }
                line = word;
            }
        }
        
        if (!line.isEmpty()) {
            this.drawString(this.fontRenderer, line, x, currentY, color);
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - SCROLL_STEP);
            this.initGui();
        } else if (wheel < 0) {
            scrollOffset = Math.min(SCROLL_STEP * 5, scrollOffset + SCROLL_STEP);
            this.initGui();
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
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
