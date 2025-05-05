package com.TNTStudios.tntcorelib.client;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.discord.DiscordPresenceHandler;
import net.fabricmc.api.ClientModInitializer;

public class TntcorelibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 🎮 Iniciar módulo Discord
        DiscordPresenceHandler.init();

        // 🎨 Iniciar módulo Custom Menu
        CustomMenuHandler.init();
    }
}
