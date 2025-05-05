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

import java.awt.Dimension;
import java.io.File;
import java.net.URI;

/**
 * Clase reducida que dibuja un video como fondo de la pantalla de carga.
 */
public final class LoadScreen {
    private static final String VIDEO_PATH = "config/TNTCore/Menu/carga.mp4";
    private static VideoPlayer player;
    private static int lastTexId;

    private LoadScreen() {}

    public static boolean tryInitVideo() {
        if (player != null) return true;
        File file = new File(VIDEO_PATH);
        if (!file.exists()) return false;
        try {
            URI uri = file.toURI();
            player = new VideoPlayer(new MediaPlayerFactory(), MinecraftClient.getInstance());
            player.start(uri);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void render(DrawContext ctx, int screenW, int screenH) {
        if (player == null) return;
        int texId = player.preRender();
        if (texId <= 0) texId = lastTexId;
        else lastTexId = texId;
        if (texId <= 0) return;

        Dimension dim = player.dimension();
        if (dim == null || dim.width <= 0 || dim.height <= 0) return;

        float vidAspect = (float) dim.width / dim.height;
        float scrAspect = (float) screenW / screenH;
        int w, h;
        if (vidAspect > scrAspect) {
            w = Math.round(screenH * vidAspect);
            h = screenH;
        } else {
            w = screenW;
            h = Math.round(screenW / vidAspect);
        }
        int x = (screenW - w) / 2;
        int y = (screenH - h) / 2;

        RenderSystem.assertOnRenderThread();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texId);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

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
    }

    public static void stopVideo() {
        if (player != null) {
            player.stop();
            player = null;
            lastTexId = 0;
        }
    }

}
