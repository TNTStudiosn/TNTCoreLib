// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/connectionmessages/ConnectionMessagesConfig.java
package com.TNTStudios.tntcorelib.modulo.connectionmessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi configuración para el módulo de mensajes de conexión.
 * Me permite activar o desactivar los mensajes de entrada y salida del servidor.
 */
public class ConnectionMessagesConfig {

    public boolean messagesEnabled = true;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Connection/connection_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración de mensajes de conexión: " + e.getMessage());
        }
    }

    public static ConnectionMessagesConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            ConnectionMessagesConfig defaultConfig = new ConnectionMessagesConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            ConnectionMessagesConfig config = GSON.fromJson(reader, ConnectionMessagesConfig.class);
            return config == null ? new ConnectionMessagesConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración de mensajes de conexión, usando valores por defecto: " + e.getMessage());
            return new ConnectionMessagesConfig();
        }
    }
}