// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/window/WindowHandler.java
package com.TNTStudios.tntcorelib.client.modulo.window;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.nio.file.Path;

/**
 * Mi manejador para el módulo de personalización de la ventana.
 * Se encarga de inicializar la configuración.
 */
public class WindowHandler {

    // Defino la ruta base para los archivos de configuración de este módulo.
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Window/");
    public static WindowConfig windowConfig;

    public static void init() {
        // Me aseguro de que la carpeta de configuración exista.
        File configFolder = CONFIG_DIR.toFile();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        // Cargo mi configuración personalizada para la ventana.
        windowConfig = WindowConfig.load();
    }
}