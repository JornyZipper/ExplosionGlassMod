package com.coders.explosion;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class VersionCheckerMod {

    private static final String CURRENT_VERSION = "1.8"; // текущая версия мода
    private static final String CURSEFORGE_LINK = "https://www.curseforge.com/minecraft/mc-mods/explosionglass";

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        // 🔹 Основное сообщение (без подчёркивания)
        TextComponentString message = new TextComponentString(
                TextFormatting.DARK_GREEN + "ExplosionGlass 1.8 " +
                        TextFormatting.WHITE + "installed. For more info: "
        );

        // 🔹 Click here — подчёркнутый, кликабельный, светло-синий
        TextComponentString clickLink = new TextComponentString("Click here");
        clickLink.getStyle().setColor(TextFormatting.BLUE); // светло-синий
        clickLink.getStyle().setUnderlined(true);           // подчёркивание
        clickLink.getStyle().setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                CURSEFORGE_LINK
        ));

        // 🔹 Добавляем Click here к основному сообщению
        message.appendSibling(clickLink);

        // 🔹 Отправляем игроку
        player.sendMessage(message);
    }
}
