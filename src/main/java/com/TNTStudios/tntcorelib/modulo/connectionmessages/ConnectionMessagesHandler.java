// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/connectionmessages/ConnectionMessagesHandler.java
package com.TNTStudios.tntcorelib.modulo.connectionmessages;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Mi manejador para el módulo de mensajes de conexión.
 * Carga la configuración y registra el comando.
 */
public class ConnectionMessagesHandler {

    private static ConnectionMessagesConfig config;

    public static void init() {
        config = ConnectionMessagesConfig.load();
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                new ConnectionMessagesCommand().register(dispatcher));
    }

    public static ConnectionMessagesConfig getConfig() {
        return config;
    }
}