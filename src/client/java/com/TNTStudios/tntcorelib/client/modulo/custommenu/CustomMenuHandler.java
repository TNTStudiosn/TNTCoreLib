// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/CustomMenuHandler.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.nio.file.Path;

public class CustomMenuHandler {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Menu/");

    public static final File LOAD_VIDEO_FILE = CONFIG_DIR.resolve("carga.mp4").toFile();
    public static final File MENU_VIDEO_FILE = CONFIG_DIR.resolve("menu.mp4").toFile();

    public static MenuConfig menuConfig;
    public static OptionsConfig optionsConfig; // ✅ NUEVO: Referencia a la config de opciones.

    public static void init() {
        File videoFolder = CONFIG_DIR.toFile();
        if (!videoFolder.exists()) {
            boolean created = videoFolder.mkdirs();
            if (created) {
                System.out.println("[TNTCoreLib] Carpeta de menú creada en: " + videoFolder.getAbsolutePath());
            }
        }

        menuConfig = MenuConfig.load();
        optionsConfig = OptionsConfig.load(); // ✅ NUEVO: Cargo la configuración de opciones.
    }
}