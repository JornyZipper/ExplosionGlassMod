package com.coders.explosion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import java.io.IOException;

public class UpdateNoticeGui extends GuiScreen {
    private final boolean bwrDetected;
    private GuiButton backButton;
    private GuiButton noShowButton;

    public UpdateNoticeGui(boolean bwrDetected) {
        this.bwrDetected = bwrDetected;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int buttonWidth = 260;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = this.height / 2;

        backButton = new GuiButton(100, centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight, I18n.format("gui.explosionglass.mainmenu"));
        this.buttonList.add(backButton);

        noShowButton = new GuiButton(101, centerX - buttonWidth / 2, startY + 26, buttonWidth, buttonHeight, I18n.format("gui.explosionglass.dontshow"));
        this.buttonList.add(noShowButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            returnToMainMenu();
        } else if (button.id == 101) {
            ExplosionGlassMod.showUpdateNotice = false;
            if (ExplosionGlassMod.config != null) {
                ExplosionGlassMod.config.get("general", "showUpdateNotice", true).set(false);
                ExplosionGlassMod.config.save();
            }
            returnToMainMenu();
        }
    }

    private void returnToMainMenu() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        if (bwrDetected) {
            drawCenteredString(this.fontRenderer, "BigWorld Detected", this.width / 2, this.height / 4 - 40, 0xFFFFFF);
            drawCenteredString(this.fontRenderer, "ExplosionGlass 2.1 does not require BigWorld.", this.width / 2, this.height / 4 - 16, 0xAAAAAA);
            drawCenteredString(this.fontRenderer, "Please remove BigWorld from your mods folder.", this.width / 2, this.height / 4 + 2, 0xAAAAAA);
            drawCenteredString(this.fontRenderer, "Then restart Minecraft and open the game again.", this.width / 2, this.height / 4 + 18, 0xAAAAAA);
            drawCenteredString(this.fontRenderer, "If you want to continue, return to the main menu.", this.width / 2, this.height / 4 + 34, 0xAAAAAA);
        } else {
            drawCenteredString(this.fontRenderer, "ExplosionGlass 1.12.2 - Final Update", this.width / 2, this.height / 4 - 40, 0xFFFFFF);
            drawCenteredString(this.fontRenderer, "This is the final update for Minecraft 1.12.2.", this.width / 2, this.height / 4 - 16, 0xAAAAAA);
            drawCenteredString(this.fontRenderer, "If you are ready, return to the main menu.", this.width / 2, this.height / 4 + 2, 0xAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            returnToMainMenu();
        }
    }
}
