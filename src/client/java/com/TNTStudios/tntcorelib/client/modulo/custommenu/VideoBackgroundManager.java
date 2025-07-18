package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;

import java.awt.Dimension;
import java.io.File;

/**
 * Mi gestor de video para los fondos de pantalla.
 * Centraliza la lógica para evitar duplicar código y gestiona los recursos de forma eficiente.
 */
public class VideoBackgroundManager {

    private final File videoFile;
    private final boolean loop;
    private VideoPlayer player;
    private int lastTexId = 0;

    public VideoBackgroundManager(File videoFile, boolean loop) {
        this.videoFile = videoFile;
        this.loop = loop;
    }

    /**
     * Intento inicializar el reproductor de video.
     * Uso el factory por defecto de la API para ser más eficiente.
     * @return true si el video se pudo iniciar, false en caso contrario.
     */
    public boolean initialize() {
        if (this.player != null) {
            return true; // Ya está inicializado
        }

        if (!videoFile.exists() || !PlayerAPI.isReady()) {
            return false;
        }

        try {
            // Uso el factory que me da la API, que ya está optimizado [cite: 1000]
            this.player = new VideoPlayer(PlayerAPI.getFactory(), MinecraftClient.getInstance());
            this.player.start(videoFile.toURI());

            // Le pongo el modo repetición si es necesario [cite: 1122]
            if (this.loop) {
                this.player.setRepeatMode(true);
            }

            return true;
        } catch (Exception e) {
            System.err.println("[TNTCoreLib] No se pudo inicializar el video: " + videoFile.getName());
            e.printStackTrace();
            // Si algo falla, libero los recursos por si acaso
            if (this.player != null) {
                this.player.release();
                this.player = null;
            }
            return false;
        }
    }

    /**
     * Renderiza el frame actual del video en toda la pantalla, manteniendo la relación de aspecto.
     */
    public void render(DrawContext ctx, int screenW, int screenH) {
        if (player == null || !player.isValid()) return;

        int texId = player.preRender();
        if (texId <= 0) {
            texId = lastTexId; // Uso el último frame válido si el actual falla
        } else {
            lastTexId = texId;
        }

        if (texId <= 0) return;

        Dimension dim = player.dimension();
        if (dim == null || dim.width <= 0 || dim.height <= 0) return;

        // Calculo la relación de aspecto para que el video llene la pantalla sin deformarse
        float videoAspect = (float) dim.width / dim.height;
        float screenAspect = (float) screenW / screenH;
        int w, h;

        if (videoAspect > screenAspect) {
            // El video es más ancho que la pantalla
            h = screenH;
            w = Math.round(screenH * videoAspect);
        } else {
            // La pantalla es más ancha que el video
            w = screenW;
            h = Math.round(screenW / videoAspect);
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

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    /**
     * Detiene y libera todos los recursos del reproductor.
     * ¡Es súper importante para no dejar basura en la memoria!
     */
    public void dispose() {
        if (player != null) {
            player.stop(); // Detengo la reproducción [cite: 1083]
            player.release(); // Libero los recursos nativos de VLC [cite: 1123]
            player = null;
            lastTexId = 0;
        }
    }
}