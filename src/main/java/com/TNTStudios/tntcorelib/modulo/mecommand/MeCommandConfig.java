// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/mecommand/MeCommandConfig.java
package com.TNTStudios.tntcorelib.modulo.mecommand;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi configuración para el módulo del comando /me.
 * Me permite activar o desactivar el comando globalmente.
 */
public class MeCommandConfig {

    public boolean commandEnabled = true;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Commands/me_command_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración del comando /me: " + e.getMessage());
        }
    }

    /**
     * Carga la configuración desde el archivo JSON.
     * Si no existe, crea uno nuevo con los valores por defecto.
     * @return una instancia de MeCommandConfig, ya sea cargada o por defecto.
     */
    public static MeCommandConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            MeCommandConfig defaultConfig = new MeCommandConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            MeCommandConfig config = GSON.fromJson(reader, MeCommandConfig.class);
            return config == null ? new MeCommandConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración del comando /me, usando valores por defecto: " + e.getMessage());
            return new MeCommandConfig();
        }
    }
}