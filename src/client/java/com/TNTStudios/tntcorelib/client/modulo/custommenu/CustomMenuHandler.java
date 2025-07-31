// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/CustomMenuHandler.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.nio.file.Path;

public class CustomMenuHandler {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Menu/");

    public static final File LOAD_VIDEO_FILE = CONFIG_DIR.resolve("carga.mp4").toFile();
    public static final File MENU_VIDEO_FILE = CONFIG_DIR.resolve("menu.mp4").toFile();
    // ✅ NUEVO: Ruta al archivo del logo del menú de pausa.
    public static File PAUSE_LOGO_FILE;

    public static MenuConfig menuConfig;
    public static OptionsConfig optionsConfig;
    // ✅ NUEVO: Referencia a la configuración del menú de pausa.
    public static PauseMenuConfig pauseMenuConfig;

    public static void init() {
        File configFolder = CONFIG_DIR.toFile();
        if (!configFolder.exists()) {
            boolean created = configFolder.mkdirs();
            if (created) {
                System.out.println("[TNTCoreLib] Carpeta de menú creada en: " + configFolder.getAbsolutePath());
            }
        }

        menuConfig = MenuConfig.load();
        optionsConfig = OptionsConfig.load();
        // ✅ NUEVO: Cargo la configuración del menú de pausa.
        pauseMenuConfig = PauseMenuConfig.load();

        // ✅ NUEVO: Defino la ruta completa al archivo del logo.
        PAUSE_LOGO_FILE = CONFIG_DIR.resolve(pauseMenuConfig.logoFileName).toFile();
    }
}