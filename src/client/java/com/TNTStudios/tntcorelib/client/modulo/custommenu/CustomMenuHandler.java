package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import java.io.File;

public class CustomMenuHandler {

    private static final File VIDEO_FOLDER = new File("config/TNTCore/Menu/");

    public static void init() {
        if (!VIDEO_FOLDER.exists()) {
            boolean created = VIDEO_FOLDER.mkdirs();
            if (created) {
                System.out.println("[TNTCoreLib] Carpeta de menú creada en: " + VIDEO_FOLDER.getAbsolutePath());
            }
        }
    }
}
