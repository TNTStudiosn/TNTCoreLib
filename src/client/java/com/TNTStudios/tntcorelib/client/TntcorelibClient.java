// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/TntcorelibClient.java
package com.TNTStudios.tntcorelib.client;

import com.TNTStudios.tntcorelib.client.modulo.antitrampas.AntiTrampasHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommodels.CustomModelsClientHandler;
import com.TNTStudios.tntcorelib.client.modulo.debug.DebugHudHandler;
import com.TNTStudios.tntcorelib.client.modulo.discord.DiscordPresenceHandler;
import com.TNTStudios.tntcorelib.client.modulo.tablist.CustomPlayerListHud;
import com.TNTStudios.tntcorelib.client.modulo.tablist.TablistHandler;
import com.TNTStudios.tntcorelib.client.modulo.window.WindowHandler;
import com.TNTStudios.tntcorelib.client.network.PlayerStatsPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TntcorelibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PlayerStatsPacketHandler.register();

        // ✅ Iniciar el receptor de paquetes para los modelos personalizados.
        CustomModelsClientHandler.registerReceivers();

        // ✨ Iniciar módulo de Ventana Personalizada
        WindowHandler.init();

        // 🎮 Iniciar módulo Discord
        DiscordPresenceHandler.init();

        // 🎨 Iniciar módulo Custom Menu
        CustomMenuHandler.init();

        // ✨ Iniciar módulo F3 Personalizado
        DebugHudHandler.init();

        // 📊 Iniciar módulo Tablist Personalizado
        TablistHandler.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                CustomPlayerListHud.tick();
            }
        });

        // 🛡️ Iniciar módulo Anti-Trampas
        AntiTrampasHandler.init();

        // ✅ Iniciar el receptor de paquetes para los modelos personalizados.
        CustomModelsClientHandler.registerReceivers();
    }
}