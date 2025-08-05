// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/custommodels/CustomModelsConfig.java
package com.TNTStudios.tntcorelib.modulo.custommodels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi configuración para el módulo de modelos personalizados.
 * Me permite definir la carpeta de los modelos y si se requieren permisos.
 */
public class CustomModelsConfig {

    public boolean requirePermission = true;
    public String modelFolderName = "player_models";

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/CustomModels/custom_models_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración de CustomModels: " + e.getMessage());
        }
    }

    public static CustomModelsConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            CustomModelsConfig defaultConfig = new CustomModelsConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            CustomModelsConfig config = GSON.fromJson(reader, CustomModelsConfig.class);
            return config == null ? new CustomModelsConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración de CustomModels, usando valores por defecto: " + e.getMessage());
            return new CustomModelsConfig();
        }
    }
}