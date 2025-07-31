// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/OptionsConfig.java
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
 * Mi clase de configuración para el menú de opciones.
 * Aquí defino qué botones estarán visibles.
 * Se guarda en un JSON para que sea fácil de editar.
 */
public class OptionsConfig {

    // Defino el estado de cada botón, por defecto todos están visibles.
    public boolean onlineButton = false;
    public boolean skinCustomizationButton = true;
    public boolean soundsButton = true;
    public boolean videoButton = true;
    public boolean controlsButton = true;
    public boolean languageButton = true;
    public boolean chatButton = false;
    public boolean resourcePackButton = true;
    public boolean accessibilityButton = false;
    public boolean telemetryButton = false;
    public boolean creditsButton = false;

    // La ruta al archivo de configuración.
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Menu/options_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Guarda la configuración actual en el archivo JSON.
     */
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración de opciones: " + e.getMessage());
        }
    }

    /**
     * Carga la configuración desde el archivo JSON.
     * Si no existe, crea uno nuevo con los valores por defecto.
     * @return una instancia de OptionsConfig, ya sea cargada o por defecto.
     */
    public static OptionsConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            OptionsConfig defaultConfig = new OptionsConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            OptionsConfig config = GSON.fromJson(reader, OptionsConfig.class);
            // Si el archivo JSON está vacío o malformado, devuelvo la config por defecto
            return config == null ? new OptionsConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración de opciones, usando valores por defecto: " + e.getMessage());
            return new OptionsConfig();
        }
    }
}