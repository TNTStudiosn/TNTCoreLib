// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/TntcorelibClient.java
package com.TNTStudios.tntcorelib.client;

import com.TNTStudios.tntcorelib.client.modulo.antitrampas.AntiTrampasHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.debug.DebugHudHandler;
import com.TNTStudios.tntcorelib.client.modulo.discord.DiscordPresenceHandler;
import com.TNTStudios.tntcorelib.client.modulo.tablist.CustomPlayerListHud;
import com.TNTStudios.tntcorelib.client.modulo.tablist.TablistHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TntcorelibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 🎮 Iniciar módulo Discord
        DiscordPresenceHandler.init();

        // 🎨 Iniciar módulo Custom Menu
        CustomMenuHandler.init();

        // ✨ Iniciar módulo F3 Personalizado
        DebugHudHandler.init(); // <-- AÑADIDO

        // 📊 Iniciar módulo Tablist Personalizado
        TablistHandler.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Me aseguro de que el mundo y el jugador existan para evitar errores al entrar o salir del juego.
            if (client.world != null && client.player != null) {
                CustomPlayerListHud.tick();
            }
        });

        // 🛡️ Iniciar módulo Anti-Trampas
        AntiTrampasHandler.init();
    }
}