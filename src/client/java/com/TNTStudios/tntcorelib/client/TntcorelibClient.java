package com.TNTStudios.tntcorelib.client;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.discord.DiscordPresenceHandler;
import com.TNTStudios.tntcorelib.client.modulo.tablist.TablistHandler;
import net.fabricmc.api.ClientModInitializer;

public class TntcorelibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 🎮 Iniciar módulo Discord
        DiscordPresenceHandler.init();

        // 🎨 Iniciar módulo Custom Menu
        CustomMenuHandler.init();

        // 📊 Iniciar módulo Tablist Personalizado
        TablistHandler.init();
    }
}
