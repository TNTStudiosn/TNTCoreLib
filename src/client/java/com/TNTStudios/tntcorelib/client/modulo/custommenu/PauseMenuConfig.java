// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/PauseMenuConfig.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi clase de configuración para el menú de pausa personalizado.
 * Aquí defino el texto de los botones y la ruta al logo.
 * Se guarda en un JSON para que sea fácil de editar.
 */
public class PauseMenuConfig {

    public String returnToGameButtonText = "Volver al Juego";
    public String optionsButtonText = "Opciones";
    public String disconnectButtonText = "Desconectar";
    public String logoFileName = "pausa_logo.png";
    public int logoRenderWidth = 128;
    public int logoRenderHeight = 128;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Menu/pause_menu_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración del menú de pausa: " + e.getMessage());
        }
    }

    public static PauseMenuConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            PauseMenuConfig defaultConfig = new PauseMenuConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            PauseMenuConfig config = GSON.fromJson(reader, PauseMenuConfig.class);
            return config == null ? new PauseMenuConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración del menú de pausa, usando valores por defecto: " + e.getMessage());
            return new PauseMenuConfig();
        }
    }
}