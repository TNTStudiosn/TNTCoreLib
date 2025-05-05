// src/main/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/LoadScreen.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.watermedia.api.player.videolan.VideoPlayer;
import org.watermedia.videolan4j.factory.MediaPlayerFactory;

import java.io.File;
import java.net.URI;
import java.awt.Dimension;

public class LoadScreen {
    // Ruta fija al video de carga
    private static final String VIDEO_PATH = "config/TNTCore/Menu/carga.mp4";
    private static final long FADE_MS = 2000;

    private static VideoPlayer player;
    private static boolean initialized = false;
    private static boolean splashDone = false;
    private static boolean videoEnded = false;
    private static boolean fading = false;
    private static long fadeStartTime = 0;

    // Intento de inicializar el reproductor (se llama una sola vez)
    public static boolean tryInitVideo() {
        if (initialized) return player != null;
        initialized = true;

        File f = new File(VIDEO_PATH);
        if (!f.exists()) {
            System.out.println("[TNTCoreLib] No existe el video de carga: " + f.getAbsolutePath());
            videoEnded = true;
            return false;
        }

        try {
            URI uri = f.toURI();
            System.out.println("[TNTCoreLib] Cargando video de carga: " + uri);
            player = new VideoPlayer(new MediaPlayerFactory(), MinecraftClient.getInstance());
            player.start(uri);
            return true;
        } catch (Exception e) {
            System.err.println("[TNTCoreLib] Error al iniciar VideoPlayer:");
            e.printStackTrace();
            videoEnded = true;
            return false;
        }
    }

    // Marca que la parte "splash" de Mojang terminó
    public static void markSplashDone() {
        splashDone = true;
    }

    // Lógica de fade y detección de fin natural
    private static void updateState() {
        if (player == null) return;

        if (!videoEnded && player.isEnded()) {
            videoEnded = true;
            System.out.println("[TNTCoreLib] El video terminó.");
        }

        if (!fading && splashDone && videoEnded) {
            fading = true;
            fadeStartTime = System.currentTimeMillis();
            System.out.println("[TNTCoreLib] Iniciando fade-out del video.");
        }

        if (fading && System.currentTimeMillis() - fadeStartTime >= FADE_MS) {
            stop();
        }
    }

    // Render del video escalado y centrado, con fade si toca
    public static void render(DrawContext ctx, int screenW, int screenH) {
        if (player == null || videoEnded && !fading) return;

        // Obtener textura y dimensiones del fotograma actual
        int texId = player.preRender();
        Dimension dim = player.dimension();
        if (dim == null) return;

        // Calcular escala manteniendo aspecto
        float aspect = (float) dim.width / dim.height;
        int w = screenW;
        int h = (int) (w / aspect);
        if (h > screenH) {
            h = screenH;
            w = (int) (h * aspect);
        }
        int x = (screenW - w) / 2;
        int y = (screenH - h) / 2;

        // Calcular alpha para fade-out
        float alpha = 1f;
        if (fading) {
            long elapsed = System.currentTimeMillis() - fadeStartTime;
            alpha = 1f - Math.min(elapsed / (float) FADE_MS, 1f);
        }

        // Preparar OpenGL
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texId);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        // Dibujar quad completo
        Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        b.vertex(mat, x,     y + h, 0f).texture(0f, 1f).next();
        b.vertex(mat, x + w, y + h, 0f).texture(1f, 1f).next();
        b.vertex(mat, x + w, y,     0f).texture(1f, 0f).next();
        b.vertex(mat, x,     y,     0f).texture(0f, 0f).next();
        Tessellator.getInstance().draw();

        // Restaurar estado GL
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        // Actualizar estado interno
        updateState();
    }

    // Detener y limpiar todo
    public static void stop() {
        if (player != null) {
            System.out.println("[TNTCoreLib] Deteniendo VideoPlayer.");
            player.stop();
            player = null;
        }
        splashDone = false;
        videoEnded = false;
        fading = false;
        initialized = false;
    }

    // Indica si ya terminó completamente (incluido fade)
    public static boolean isFinished() {
        return player == null && initialized == false;
    }
}
