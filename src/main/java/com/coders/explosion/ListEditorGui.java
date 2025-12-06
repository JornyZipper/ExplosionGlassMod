package com.coders.explosion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Arrays;

public class ListEditorGui extends GuiScreen {
    
    private GuiScreen parent;
    private String title;
    private String[] list;
    private boolean isBlacklist;
    
    private GuiTextField inputField;
    private GuiButton addButton;
    private GuiButton removeButton;
    private GuiButton doneButton;
    
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 20;
    private static final int MAX_VISIBLE = 10;
    
    public ListEditorGui(GuiScreen parent, String title, String[] list, boolean isBlacklist) {
        this.parent = parent;
        this.title = title;
        this.list = Arrays.copyOf(list, list.length);
        this.isBlacklist = isBlacklist;
    }
    
    @Override
    public void initGui() {
        this.buttonList.clear();
        
        this.inputField = new GuiTextField(1, this.fontRenderer, 20, 40, 300, 20);
        this.inputField.setMaxStringLength(100);
        
        this.addButton = new GuiButton(100, 20, 70, 100, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.add"));
        this.removeButton = new GuiButton(101, 130, 70, 150, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.removeselected"));
        this.doneButton = new GuiButton(200, this.width / 2 - 50, this.height - 30, 100, 20, net.minecraft.client.resources.I18n.format("gui.explosionglass.done"));
        
        this.buttonList.add(addButton);
        this.buttonList.add(removeButton);
        this.buttonList.add(doneButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 100) {
            // Add
            String input = inputField.getText().trim();
            if (!input.isEmpty()) {
                String[] newList = Arrays.copyOf(list, list.length + 1);
                newList[list.length] = input;
                list = newList;
                inputField.setText("");
                selectedIndex = -1;
            }
        } else if (button.id == 101) {
            // Remove
            if (selectedIndex >= 0 && selectedIndex < list.length) {
                String[] newList = new String[list.length - 1];
                int idx = 0;
                for (int i = 0; i < list.length; i++) {
                    if (i != selectedIndex) {
                        newList[idx++] = list[i];
                    }
                }
                list = newList;
                selectedIndex = -1;
            }
        } else if (button.id == 200) {
            // Done - save and return
            if (isBlacklist) {
                ExplosionGlassMod.glassBlacklist = list;
            } else {
                ExplosionGlassMod.glassWhitelist = list;
            }
            this.mc.displayGuiScreen(parent);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, title + " " + net.minecraft.client.resources.I18n.format("gui.explosionglass.editor"), this.width / 2, 20, 0xFFFFFF);
        
        this.drawString(this.fontRenderer, net.minecraft.client.resources.I18n.format("gui.explosionglass.blockname") + ":", 20, 30, 0xFFFFFF);
        this.inputField.drawTextBox();
        
        this.drawString(this.fontRenderer, net.minecraft.client.resources.I18n.format("gui.explosionglass.itemsinlist") + ":", 20, 100, 0xFFFFFF);
        
        // Draw list items
        int yPos = 120;
        for (int i = scrollOffset; i < Math.min(scrollOffset + MAX_VISIBLE, list.length); i++) {
            if (i == selectedIndex) {
                this.drawRect(20, yPos, 320, yPos + ITEM_HEIGHT, 0xFF6699CC);
            }
            this.drawString(this.fontRenderer, (i + 1) + ". " + list[i], 30, yPos + 5, 0xFFFFFF);
            yPos += ITEM_HEIGHT;
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        inputField.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Select item from list
        int yPos = 120;
        for (int i = scrollOffset; i < Math.min(scrollOffset + MAX_VISIBLE, list.length); i++) {
            if (mouseX >= 20 && mouseX <= 320 && mouseY >= yPos && mouseY < yPos + ITEM_HEIGHT) {
                selectedIndex = i;
                break;
            }
            yPos += ITEM_HEIGHT;
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (wheel < 0) {
            scrollOffset = Math.min(Math.max(0, list.length - MAX_VISIBLE), scrollOffset + 1);
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        inputField.textboxKeyTyped(typedChar, keyCode);
        
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(parent);
        }
    }
    
    @Override
    public void onGuiClosed() {
        // Save lists when closing
        if (isBlacklist) {
            ExplosionGlassMod.glassBlacklist = list;
        } else {
            ExplosionGlassMod.glassWhitelist = list;
        }
    }
}
