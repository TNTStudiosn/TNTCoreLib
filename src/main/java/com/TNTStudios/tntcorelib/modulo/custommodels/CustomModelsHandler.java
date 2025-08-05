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

    public static void init(MinecraftServer server) {
        config = CustomModelsConfig.load();
        customModelsManager = new CustomModelsManager(server, config);

        // Actualizo el registro del comando. Ya no le paso la config porque la
        // comprobación de permisos ahora es fija y no configurable.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CustomModelsCommand.register(dispatcher));
    }

    public static CustomModelsManager getManager() {
        return customModelsManager;
    }
}