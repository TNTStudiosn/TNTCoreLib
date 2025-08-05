// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/timer/TimerHandler.java
package com.TNTStudios.tntcorelib.modulo.timer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;

/**
 * Mi manejador para el módulo del Temporizador.
 * Se encarga de inicializar el manager y registrar los comandos
 * en el momento adecuado del ciclo de vida del servidor.
 * No necesita cambios, su lógica sigue siendo correcta.
 */
public class TimerHandler {

    private static TimerManager timerManager;

    /**
     * Inicializa el manager del temporizador. Lo llamo desde el evento
     * SERVER_STARTING para asegurar que tengo la instancia del servidor.
     * @param server La instancia del servidor de Minecraft.
     */
    public static void initialize(MinecraftServer server) {
        if (timerManager == null) {
            timerManager = new TimerManager(server);
        }
    }

    /**
     * Registra los comandos del módulo. Lo llamo desde onInitialize.
     */
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                new TimerCommand().register(dispatcher));
    }

    /**
     * Devuelve la instancia del manager, que implementa mi TimerApi.
     * @return La instancia del TimerManager.
     */
    public static TimerManager getManager() {
        return timerManager;
    }
}