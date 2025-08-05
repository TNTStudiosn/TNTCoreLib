// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/custommodels/CustomModelsHandler.java
package com.TNTStudios.tntcorelib.modulo.custommodels;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;

/**
 * Mi manejador principal para el módulo de Custom Models.
 * Se encarga de cargar la config, registrar la API y los comandos.
 */
public class CustomModelsHandler {

    private static CustomModelsManager customModelsManager;
    private static CustomModelsConfig config;

    /**
     * ✅ CORREGIDO: Mi método para registrar los comandos.
     * Lo llamo directamente desde la clase principal del mod en 'onInitialize'
     * para asegurar que los comandos se registren en el momento correcto del ciclo de vida de Fabric.
     */
    public static void registerCommands() {
        // Registro el evento que registrará mi comando cuando el servidor esté listo para ello.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CustomModelsCommand.register(dispatcher));
    }

    /**
     * ✅ CORREGIDO: Renombro 'init' a 'initializeManager' para que quede más claro.
     * Este método inicializa la lógica que SÍ depende de la instancia del servidor
     * y por eso lo llamo dentro del evento 'ServerLifecycleEvents.SERVER_STARTING'.
     * @param server La instancia del servidor de Minecraft.
     */
    public static void initializeManager(MinecraftServer server) {
        config = CustomModelsConfig.load();
        customModelsManager = new CustomModelsManager(server, config);
    }

    public static CustomModelsManager getManager() {
        return customModelsManager;
    }
}