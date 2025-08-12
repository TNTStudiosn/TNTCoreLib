// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/chatcontrol/ChatControlHandler.java
package com.TNTStudios.tntcorelib.modulo.chatcontrol;

import com.TNTStudios.tntcorelib.api.chat.ChatControlApi;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Mi manejador para el módulo de control de chat.
 * Se encarga de cargar la configuración, inicializar el manager y registrar el comando.
 */
public class ChatControlHandler {

    private static ChatControlManager manager;

    public static void init() {
        manager = new ChatControlManager();
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                new ChatControlCommand().register(dispatcher));
    }

    public static ChatControlApi getApi() {
        return manager;
    }
}