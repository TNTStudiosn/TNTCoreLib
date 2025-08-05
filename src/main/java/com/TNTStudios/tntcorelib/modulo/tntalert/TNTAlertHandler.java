// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/tntalert/TNTAlertHandler.java
package com.TNTStudios.tntcorelib.modulo.tntalert;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;

/**
 * Mi manejador para el módulo de Alertas.
 * Se encarga de inicializar el manager y registrar los comandos,
 * manteniendo la clase principal del mod limpia y ordenada.
 */
public class TNTAlertHandler {

    private static TNTAlertManager alertManager;

    /**
     * Inicializa el manager de alertas cuando el servidor arranca.
     * @param server La instancia del servidor.
     */
    public static void initializeManager(MinecraftServer server) {
        if (alertManager == null) {
            alertManager = new TNTAlertManager(server);
        }
    }

    /**
     * Registra los comandos del módulo. Lo llamo desde onInitialize.
     */
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                new TNTAlertCommand().register(dispatcher));
    }

    /**
     * Devuelve la instancia del manager, que implementa mi TNTAlertApi.
     * @return La instancia del TNTAlertManager.
     */
    public static TNTAlertManager getManager() {
        return alertManager;
    }
}