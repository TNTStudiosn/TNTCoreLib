package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.watermedia.api.player.videolan.VideoPlayer;
import org.watermedia.videolan4j.factory.MediaPlayerFactory;

import java.awt.Dimension;
import java.io.File;
import java.net.URI;

public class LoadScreen {

    private static VideoPlayer player;
    private static boolean initialized = false;

    // --- Flags para controlar el fade tras el cierre del splash ---
    private static boolean splashEnded = false;
    private static boolean fading = false;
    private static long fadeStartMs = 0;
    private static final long FADE_DURATION_MS = 2000; // 1 s de fade

    public static boolean tryInitVideo() {
        if (initialized) return player != null;

        File videoFile = new File("config/TNTCore/Menu/carga.mp4");
        if (!videoFile.exists()) {
            System.out.println("[TNTCoreLib] No se encontró el video de carga: " + videoFile.getAbsolutePath());
            initialized = true;
            return false;
        }

        try {
            URI uri = videoFile.toURI();
            System.out.println("[TNTCoreLib] Iniciando VideoPlayer con: " + uri);
            player = new VideoPlayer(new MediaPlayerFactory(), MinecraftClient.getInstance());
            player.start(uri);
        } catch (Exception e) {
            System.err.println("[TNTCoreLib] Error al iniciar el video de carga:");
            e.printStackTrace();
        }

        initialized = true;
        return player != null;
    }

    /** Llamar desde el mixin cuando el splash de Mojang se cierre */
    public static void requestFade() {
        if (!splashEnded) {
            splashEnded = true;
            System.out.println("[TNTCoreLib] Splash terminado: pendiente fade-out.");
        }
    }

    /** Gestiona cuándo arrancar el fade y cuándo detener realmente el vídeo */
    private static void tickFade() {
        if (player == null || !splashEnded) return;

        // Si aún no hemos empezado el fade...
        if (!fading) {
            // Solo iniciamos fade cuando el vídeo ya haya terminado
            if (!player.isEnded()) {
                return; // esperamos a fin del vídeo
            }
            fading = true;
            fadeStartMs = System.currentTimeMillis();
        }

        // Si ya estamos en fade, comprobamos duración
        long elapsed = System.currentTimeMillis() - fadeStartMs;
        if (elapsed >= FADE_DURATION_MS) {
            stop();
            player = null;
            // reseteamos banderas para posible recarga
            splashEnded = false;
            fading = false;
        }
    }

    /** Dibuja el vídeo, con alpha si estamos en fade */
    public static void render(DrawContext context, int screenW, int screenH) {
        if (player == null) return;

        int texId = player.preRender();
        Dimension dim = player.dimension();
        if (dim == null) return;

        // calculamos escalado manteniendo aspect ratio
        float aspect = (float) dim.width / dim.height;
        int w = screenW;
        int h = (int) (w / aspect);
        if (h > screenH) {
            h = screenH;
            w = (int) (h * aspect);
        }
        int x = (screenW - w) / 2;
        int y = (screenH - h) / 2;

        // calculamos alpha en función de si ya estamos en fade
        float alpha = 1f;
        if (fading) {
            long elapsed = System.currentTimeMillis() - fadeStartMs;
            alpha = 1f - Math.min(elapsed / (float) FADE_DURATION_MS, 1f);
        }

        // configuramos GL/shader
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texId);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        // dibujamos el quad centrado
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        builder.vertex(matrix, x,     y + h, 0f).texture(0f, 1f).next();
        builder.vertex(matrix, x + w, y + h, 0f).texture(1f, 1f).next();
        builder.vertex(matrix, x + w, y,     0f).texture(1f, 0f).next();
        builder.vertex(matrix, x,     y,     0f).texture(0f, 0f).next();
        Tessellator.getInstance().draw();

        // restauramos GL
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        // avanzamos el fade si procede
        tickFade();
    }

    public static void stop() {
        if (player != null) {
            System.out.println("[TNTCoreLib] Deteniendo video de carga...");
            player.stop();
        }
    }
}
