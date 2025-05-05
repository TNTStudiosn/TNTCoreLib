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
    private static final String VIDEO_PATH = "config/TNTCore/Menu/carga.mp4";
    private static final long FADE_MS = 2000;

    private static VideoPlayer player;
    private static boolean initialized = false;
    private static boolean splashDone = false;
    private static boolean videoEnded = false;
    private static boolean fading = false;
    private static long fadeStartTime = 0;

    public static boolean tryInitVideo() {
        if (initialized) return player != null;
        initialized = true;

        File f = new File(VIDEO_PATH);
        if (!f.exists()) {
            System.out.println("[TNTCoreLib] No existe el video: " + f.getAbsolutePath());
            videoEnded = true;
            return false;
        }

        try {
            URI uri = f.toURI();
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

    public static void markSplashDone() {
        splashDone = true;
    }

    private static void updateState() {
        if (player == null) return;

        if (!videoEnded && player.isEnded()) {
            videoEnded = true;
        }

        if (!fading && splashDone && videoEnded) {
            fading = true;
            fadeStartTime = System.currentTimeMillis();
        }

        if (fading && System.currentTimeMillis() - fadeStartTime >= FADE_MS) {
            stop();
            MinecraftClient.getInstance().setOverlay(null); // fuerza cerrar overlay
        }
    }

    public static void render(DrawContext ctx, int screenW, int screenH) {
        if (player == null || (videoEnded && !fading)) return;

        int texId = player.preRender();
        if (texId <= 0) return;

        Dimension dim = player.dimension();
        if (dim == null || dim.width == 0 || dim.height == 0) return;

        // Fullscreen con aspecto corregido y fijo al centro
        float videoAspect = (float) dim.width / dim.height;
        float screenAspect = (float) screenW / screenH;

        int w = screenW, h = screenH;
        if (videoAspect > screenAspect) {
            h = (int) (screenW / videoAspect);
        } else {
            w = (int) (screenH * videoAspect);
        }
        int x = (screenW - w) / 2;
        int y = (screenH - h) / 2;

        float alpha = 1f;
        if (fading) {
            long elapsed = System.currentTimeMillis() - fadeStartTime;
            alpha = 1f - Math.min(elapsed / (float) FADE_MS, 1f);
        }

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texId);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        b.vertex(mat, x,     y + h, 0f).texture(0f, 1f).next();
        b.vertex(mat, x + w, y + h, 0f).texture(1f, 1f).next();
        b.vertex(mat, x + w, y,     0f).texture(1f, 0f).next();
        b.vertex(mat, x,     y,     0f).texture(0f, 0f).next();
        Tessellator.getInstance().draw();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        updateState();
    }

    public static void stop() {
        if (player != null) {
            player.stop();
            player = null;
        }
        splashDone = false;
        videoEnded = false;
        fading = false;
        initialized = false;
    }

    public static boolean isFinished() {
        return player == null && !initialized;
    }

    private LoadScreen() {}
}