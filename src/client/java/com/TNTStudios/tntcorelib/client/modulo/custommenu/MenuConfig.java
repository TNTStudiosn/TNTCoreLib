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
 * Mi clase de configuración para el menú principal personalizado.
 * Aquí defino la IP del servidor, el texto de los botones y la versión del diseño.
 * Se guarda en un JSON para que sea fácil de editar por el usuario final.
 */
public class MenuConfig {

    // Defino las variables de configuración con valores por defecto.
    public String serverIp = "mc.servidorejemplo.com";
    public int layoutVersion = 1; // Un número del 1 al 5.
    public String entrarButtonText = "Entrar"; // ✅ NUEVO: Texto personalizable para el botón de entrar.

    // La ruta al archivo de configuración.
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Menu/menu_config.json");
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
            System.err.println("[TNTCoreLib] No pude guardar la configuración del menú: " + e.getMessage());
        }
    }

    /**
     * Carga la configuración desde el archivo JSON.
     * Si no existe, crea uno nuevo con los valores por defecto.
     * @return una instancia de MenuConfig, ya sea cargada o por defecto.
     */
    public static MenuConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            MenuConfig defaultConfig = new MenuConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            MenuConfig config = GSON.fromJson(reader, MenuConfig.class);
            if (config == null) {
                return new MenuConfig();
            }
            return config;
        } catch (IOException e) {
            System.err.println("[TNTCoreLib] No pude leer la configuración del menú, usando valores por defecto: " + e.getMessage());
            return new MenuConfig();
        }
    }
}