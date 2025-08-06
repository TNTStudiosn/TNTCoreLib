// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/playerstats/PlayerStatsHandler.java
package com.TNTStudios.tntcorelib.modulo.playerstats;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;

/**
 * Mi manejador para el módulo de estadísticas de jugador.
 * Se encarga de inicializar el manager y registrar los comandos.
 */
public class PlayerStatsHandler {

    private static PlayerStatsManager statsManager;

    /**
     * Inicializa el manager cuando el servidor arranca.
     * @param server La instancia del servidor.
     */
    public static void initializeManager(MinecraftServer server) {
        if (statsManager == null) {
            statsManager = new PlayerStatsManager();
        }
    }

    /**
     * Registra los comandos del módulo. Lo llamo desde onInitialize.
     */
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                new PlayerStatsCommand().register(dispatcher));
    }

    /**
     * Devuelve la instancia del manager, que implementa mi PlayerStatsApi.
     * @return La instancia del PlayerStatsManager.
     */
    public static PlayerStatsManager getManager() {
        return statsManager;
    }
}