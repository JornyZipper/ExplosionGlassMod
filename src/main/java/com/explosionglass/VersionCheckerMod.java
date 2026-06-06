package com.explosionglass;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerPlayer;

public class VersionCheckerMod {

    private static final String CURSEFORGE_LINK = "https://www.curseforge.com/minecraft/mc-mods/explosionglass";

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();

        // Основное сообщение
        Component message = Component.literal("\u00a72EXPLGlass 0.1.0 \u00a7finstalled. For more info: ");

        // Click here с кликом
        Component clickHere = Component.literal("Click here")
                .withStyle(style -> style.withColor(0x0055FF).withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CURSEFORGE_LINK)));

        // Добавляем clickHere к основному сообщению
        Component fullMessage = message.copy().append(clickHere);

        // Отправляем игроку
        player.displayClientMessage(fullMessage, false);
    }
}
