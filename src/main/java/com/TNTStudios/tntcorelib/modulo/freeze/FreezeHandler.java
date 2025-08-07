// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/freeze/FreezeHandler.java
package com.TNTStudios.tntcorelib.modulo.freeze;

import com.TNTStudios.tntcorelib.api.freeze.FreezeApi;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.UUID;

/**
 * Mi manejador para el módulo de Freeze.
 * AHORA: Expone métodos estáticos para ser llamados desde la clase principal,
 * desacoplando el registro de eventos y la inicialización.
 */
public class FreezeHandler {

    private static FreezeManager manager;
    private static final int SAVE_INTERVAL_TICKS = 20 * 60 * 5; // Guardo cada 5 minutos.

    /**
     * Mi método para registrar los comandos del módulo Freeze.
     * Debe ser público para poder llamarlo desde la clase principal del mod.
     */
    public static void registerCommands() {
        // Registro el comando del módulo.
        // ✅ CORRECCIÓN: Creo una nueva instancia de FreezeCommand para registrarla.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                new FreezeCommand().register(dispatcher));
    }

    /**
     * Mi método para inicializar el manager y los eventos de ciclo de vida.
     * Carga la configuración, crea la instancia del manager y registra los eventos
     * de guardado para garantizar la persistencia de datos.
     * Lo llamo desde el evento SERVER_STARTING en mi clase principal.
     */
    public static void initialize(MinecraftServer server) {
        // Primero creo el manager, que internamente cargará su estado.
        manager = new FreezeManager();

        // Registro el guardado final y síncrono para cuando el servidor se detiene.
        // Esto es crítico para no perder datos.
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            if (manager != null) {
                System.out.println("[TNTCoreLib] Guardando datos de jugadores congelados antes de apagar...");
                manager.saveState(false); // Guardado síncrono y bloqueante.
            }
        });

        // Registro el guardado periódico y asíncrono para no generar lag.
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            // Solo ejecuto en el tick correcto y si el manager está activo.
            if (s.getTicks() % SAVE_INTERVAL_TICKS == 0 && manager != null) {
                manager.saveState(true); // Guardo de forma asíncrona en segundo plano.
            }
        });
    }

    /**
     * Mi método para obtener la instancia de la API pública.
     * Así expongo la funcionalidad de forma segura a otros módulos o mods.
     * @return La instancia de la API, o una implementación nula si no se ha inicializado.
     */
    public static FreezeApi getApi() {
        if (manager == null) {
            // ✅ CORRECCIÓN: Devuelvo una implementación nula si la API se solicita antes de tiempo.
            // Esto evita NullPointerExceptions y mantiene el servidor estable.
            return new FreezeApi() {
                @Override public void freezePlayer(ServerPlayerEntity player) {}
                @Override public void freezePlayer(Collection<ServerPlayerEntity> players) {}
                @Override public void unfreezePlayer(ServerPlayerEntity player) {}
                @Override public void unfreezePlayer(Collection<ServerPlayerEntity> players) {}
                @Override public boolean isPlayerFrozen(ServerPlayerEntity player) { return false; }
                @Override public boolean isPlayerFrozen(UUID playerUuid) { return false; }
            };
        }
        return manager;
    }
}