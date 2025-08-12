// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/chatcontrol/ChatControlConfig.java
package com.TNTStudios.tntcorelib.modulo.chatcontrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Mi configuración para el módulo de control de chat.
 * Almacena si el chat global debe estar silenciado por defecto.
 */
public class ChatControlConfig {

    public boolean chatGloballyMuted = false;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/ChatControl/chat_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración de ChatControl: " + e.getMessage());
        }
    }

    public static ChatControlConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            ChatControlConfig defaultConfig = new ChatControlConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            ChatControlConfig config = GSON.fromJson(reader, ChatControlConfig.class);
            return config == null ? new ChatControlConfig() : config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración de ChatControl, usando valores por defecto: " + e.getMessage());
            return new ChatControlConfig();
        }
    }
}