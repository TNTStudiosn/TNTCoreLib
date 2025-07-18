package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.nio.file.Path;

public class CustomMenuHandler {

    // Uso el directorio de config de Fabric para que todo esté donde debe
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("TNTCore/Menu/");

    // Defino las rutas de los videos aquí para tener un único punto de referencia
    public static final File LOAD_VIDEO_FILE = CONFIG_DIR.resolve("carga.mp4").toFile();
    public static final File MENU_VIDEO_FILE = CONFIG_DIR.resolve("menu.mp4").toFile();

    public static void init() {
        File videoFolder = CONFIG_DIR.toFile();
        if (!videoFolder.exists()) {
            boolean created = videoFolder.mkdirs();
            if (created) {
                System.out.println("[TNTCoreLib] Carpeta de menú creada en: " + videoFolder.getAbsolutePath());
            }
        }
    }
}