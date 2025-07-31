// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/window/WindowConfig.java
package com.TNTStudios.tntcorelib.client.modulo.window;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi clase de configuración para la personalización de la ventana del juego.
 * Me permite cambiar el título y el icono desde un archivo JSON.
 */
public class WindowConfig {

    // Controles para activar o desactivar cada función por separado.
    public boolean useCustomTitle = true;
    public boolean useCustomIcon = true;

    // Valores por defecto que el usuario puede cambiar.
    public String windowTitle = "TNTStudios";
    public String icon16 = "icon_16x16.png";
    public String icon32 = "icon_32x32.png";
    public String icon256 = "icon_256x256.png";

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Window/window_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void save() {
        try {
            File configFile = CONFIG_PATH.toFile();
            if (configFile.getParentFile() != null) {
                configFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude guardar la configuración de la ventana: " + e.getMessage());
        }
    }

    public static WindowConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            WindowConfig defaultConfig = new WindowConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            WindowConfig config = GSON.fromJson(reader, WindowConfig.class);
            return config == null ? new WindowConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración de la ventana, usando valores por defecto: " + e.getMessage());
            return new WindowConfig();
        }
    }
}