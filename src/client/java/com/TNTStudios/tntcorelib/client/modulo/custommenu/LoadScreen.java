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

/**
 * Clase que muestra un video de carga superpuesto a la pantalla de inicio.
 * - Mantiene el último frame si el video termina antes de la carga.
 * - Ejecuta fade-out tras carga y fin de video.
 * - Frame dropping para PCs de bajo rendimiento.
 */
public final class LoadScreen {
    private static final String VIDEO_PATH = "config/TNTCore/Menu/carga.mp4";
    private static final long FADE_MS = 2000;
    private static final int TARGET_FPS = 30;
    private static final long FRAME_INTERVAL = 1000L / TARGET_FPS;

    private static VideoPlayer player;
    private static boolean initialized = false;
    private static boolean splashDone = false;
    private static boolean videoEnded = false;
    private static boolean fading = false;
    private static long fadeStartTime = 0;
    private static int lastTexId = 0;
    private static long lastFrameTime = 0;

    /**
     * Inicializa el VideoPlayer solo una vez.
     * @return true si el video pudo cargarse
     */
    public static boolean tryInitVideo() {
        if (initialized) return player != null;
        initialized = true;
        File file = new File(VIDEO_PATH);
        if (!file.exists()) {
            videoEnded = true;
            return false;
        }
        try {
            URI uri = file.toURI();
            player = new VideoPlayer(new MediaPlayerFactory(), MinecraftClient.getInstance());
            player.start(uri);
            lastFrameTime = System.currentTimeMillis();
            return true;
        } catch (Exception e) {
            videoEnded = true;
            return false;
        }
    }

    /** Marca que la pantalla de Mojang ha terminado su splash. */
    public static void markSplashDone() {
        splashDone = true;
    }

    /** Actualiza estado interno y controla fade y cierre. */
    private static void updateState() {
        if (!videoEnded && player != null && player.isEnded()) {
            videoEnded = true;
        }
        if (splashDone && videoEnded && !fading) {
            fading = true;
            fadeStartTime = System.currentTimeMillis();
        }
        if (fading && (System.currentTimeMillis() - fadeStartTime) >= FADE_MS) {
            stop();
            MinecraftClient.getInstance().setOverlay(null);
        }
    }

    /**
     * Renderiza el video cubriendo toda la pantalla (modo cover),
     * con fade-out y frame dropping para rendimiento.
     */
    public static void render(DrawContext ctx, int screenW, int screenH) {
        if (player == null) return;
        long now = System.currentTimeMillis();

        // Avanza frame limitando FPS
        if (!videoEnded && now - lastFrameTime >= FRAME_INTERVAL) {
            int id = player.preRender();
            if (id > 0) lastTexId = id;
            lastFrameTime = now;
        }
        int texId = lastTexId;
        if (texId <= 0) return;

        Dimension dim = player.dimension();
        if (dim == null || dim.width <= 0 || dim.height <= 0) return;

        // Calcular escala modo cover (llenar pantalla)
        float vidAspect = (float) dim.width / dim.height;
        float scrAspect = (float) screenW / screenH;
        int w, h;
        if (vidAspect > scrAspect) {
            h = screenH;
            w = Math.round(screenH * vidAspect);
        } else {
            w = screenW;
            h = Math.round(screenW / vidAspect);
        }
        int x = (screenW - w) / 2;
        int y = (screenH - h) / 2;

        // Calcular alpha para fade
        float alpha = 1f;
        if (fading) {
            alpha = 1f - Math.min((now - fadeStartTime) / (float) FADE_MS, 1f);
        }

        // Preparar render
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texId);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        // Dibujar quad con matrix stack (cover)
        Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(mat, x,     y + h, 0f).texture(0f, 1f).next();
        buf.vertex(mat, x + w, y + h, 0f).texture(1f, 1f).next();
        buf.vertex(mat, x + w, y,     0f).texture(1f, 0f).next();
        buf.vertex(mat, x,     y,     0f).texture(0f, 0f).next();
        Tessellator.getInstance().draw();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        // Actualizar estado (fade & cierre)
        updateState();
    }

    /** Detiene y libera video. */
    public static void stop() {
        if (player != null) {
            player.stop();
            player = null;
        }
        initialized = false;
        splashDone = false;
        videoEnded = false;
        fading = false;
    }

    /** True si ya terminó y debe cerrarse overlay. */
    public static boolean isFinished() {
        return player == null && !initialized;
    }

    private LoadScreen() {}
}