package com.coders.explosion;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class VersionCheckerMod {

    private static final String CURRENT_VERSION = "1.9.2";
    private static final String CURSEFORGE_LINK = "https://www.curseforge.com/minecraft/mc-mods/explosionglass";

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;

        // Основное сообщение
        TextComponentString message = new TextComponentString(TextFormatting.DARK_GREEN + "ExplosionGlass 1.9.2 "
                + TextFormatting.WHITE + "installed. For more info: ");

        // Click here с кликом
        TextComponentString clickHere = new TextComponentString("Click here");
        clickHere.getStyle().setColor(TextFormatting.BLUE);
        clickHere.getStyle().setUnderlined(true);
        clickHere.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CURSEFORGE_LINK));

        // Добавляем clickHere к основному сообщению
        message.appendSibling(clickHere);

        // Отправляем игроку
        player.sendMessage(message);
    }
}
