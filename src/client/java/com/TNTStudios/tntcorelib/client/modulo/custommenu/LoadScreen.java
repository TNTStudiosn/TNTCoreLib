package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.minecraft.client.gui.DrawContext;

/**
 * Mi clase para la pantalla de carga, ahora mucho más limpia.
 * Solo se encarga de llamar al gestor de video.
 */
public final class LoadScreen {
    private static VideoBackgroundManager videoManager;

    private LoadScreen() {}

    public static void tryInitVideo() {
        if (videoManager == null) {
            // Inicio el video sin loop
            videoManager = new VideoBackgroundManager(CustomMenuHandler.LOAD_VIDEO_FILE, false);
            if (!videoManager.initialize()) {
                // Si falla, lo pongo a null para que no intente renderizar nada
                videoManager = null;
            }
        }
    }

    public static void render(DrawContext ctx, int screenW, int screenH) {
        if (videoManager != null) {
            videoManager.render(ctx, screenW, screenH);
        }
    }

    public static void stopVideo() {
        if (videoManager != null) {
            // Me aseguro de liberar todos los recursos cuando la pantalla de carga termina
            videoManager.dispose();
            videoManager = null;
        }
    }
}