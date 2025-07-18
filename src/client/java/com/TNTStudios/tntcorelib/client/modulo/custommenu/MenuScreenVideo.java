package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.minecraft.client.gui.DrawContext;

/**
 * Mi clase para el video del menú principal, ahora también súper simple.
 */
public final class MenuScreenVideo {
    private static VideoBackgroundManager videoManager;

    private MenuScreenVideo() {}

    public static void tryInitVideo() {
        if (videoManager == null) {
            // Inicio el video CON loop
            videoManager = new VideoBackgroundManager(CustomMenuHandler.MENU_VIDEO_FILE, true);
            if (!videoManager.initialize()) {
                videoManager = null;
            }
        }
    }

    public static void render(DrawContext ctx, int screenW, int screenH) {
        if (videoManager != null) {
            videoManager.render(ctx, screenW, screenH);
        }
    }

    public static void stop() {
        if (videoManager != null) {
            // Libero los recursos cuando se cierra la pantalla de título
            videoManager.dispose();
            videoManager = null;
        }
    }
}