// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/debug/DebugHudConfig.java
package com.TNTStudios.tntcorelib.client.modulo.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi clase de configuración para el HUD de depuración (F3).
 * Aquí defino el texto personalizable que aparecerá en la parte superior.
 */
public class DebugHudConfig {

    public String brandText = "TNTCoreLib";

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Debug/debug_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración del Debug HUD: " + e.getMessage());
        }
    }

    public static DebugHudConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            DebugHudConfig defaultConfig = new DebugHudConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            DebugHudConfig config = GSON.fromJson(reader, DebugHudConfig.class);
            return config == null ? new DebugHudConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración del Debug HUD, usando valores por defecto: " + e.getMessage());
            return new DebugHudConfig();
        }
    }
}