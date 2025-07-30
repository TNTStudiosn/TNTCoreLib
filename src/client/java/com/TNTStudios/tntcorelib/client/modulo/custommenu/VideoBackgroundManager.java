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

    // Defino el aspect ratio del contenido del video como una constante (16:9).
    // Esto es clave para diferenciarlo del aspect ratio del frame completo (que puede tener barras).
    private static final float CONTENT_ASPECT_RATIO = 16.0f / 9.0f;

    public VideoBackgroundManager(File videoFile, boolean loop) {
        this.videoFile = videoFile;
        this.loop = loop;
    }

    public boolean initialize() {
        if (this.player != null) return true;
        if (!videoFile.exists() || !PlayerAPI.isReady()) return false;

        try {
            this.player = new VideoPlayer(PlayerAPI.getFactory(), MinecraftClient.getInstance());
            this.player.start(videoFile.toURI());
            if (this.loop) {
                this.player.setRepeatMode(true);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[TNTCoreLib] No se pudo inicializar el video: " + videoFile.getName());
            e.printStackTrace();
            if (this.player != null) {
                this.player.release();
                this.player = null;
            }
            return false;
        }
    }

    /**
     * Renderiza el frame actual del video, cubriendo toda la pantalla, sin deformaciones y
     * corrigiendo el posible letterboxing/pillarboxing del archivo de video.
     */
    public void render(DrawContext ctx, int screenW, int screenH) {
        if (player == null || !player.isValid()) return;

        int texId = player.preRender();
        if (texId <= 0) texId = lastTexId; else lastTexId = texId;
        if (texId <= 0) return;

        // Asumo que `player.dimension()` me da las dimensiones del frame completo del video (incluyendo barras negras).
        Dimension textureDim = player.dimension();
        if (textureDim == null || textureDim.width <= 0 || textureDim.height <= 0) {
            return;
        }

        // === LÓGICA DE RENDERIZADO AVANZADA ===

        // 1. Calculo el aspect ratio de la pantalla.
        float screenAspect = (float) screenW / (float) screenH;

        // 2. Calculo las dimensiones del quad para que cubra la pantalla usando el aspect ratio del CONTENIDO.
        //    Esta es tu lógica original, que funciona perfectamente para el efecto "cover".
        float drawW, drawH, x, y;
        if (screenAspect > CONTENT_ASPECT_RATIO) {
            // La pantalla es más ancha: el ancho del video debe ser igual al de la pantalla.
            drawW = screenW;
            drawH = screenW / CONTENT_ASPECT_RATIO;
            x = 0;
            y = (screenH - drawH) / 2.0f;
        } else {
            // La pantalla es más alta: el alto del video debe ser igual al de la pantalla.
            drawH = screenH;
            drawW = screenH * CONTENT_ASPECT_RATIO;
            y = 0;
            x = (screenW - drawW) / 2.0f;
        }

        // 3. ¡La clave! Ajusto las coordenadas UV para "recortar" las barras negras del video.
        //    Comparo el aspect ratio del frame completo con el del contenido real.
        float textureAspect = (float) textureDim.width / (float) textureDim.height;
        float u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f;

        if (textureAspect > CONTENT_ASPECT_RATIO) {
            // El frame es más ancho que el contenido -> Pillarbox (barras a los lados).
            float expectedContentWidth = CONTENT_ASPECT_RATIO * textureDim.height;
            float barWidth = (textureDim.width - expectedContentWidth) / 2.0f;
            u0 = barWidth / textureDim.width;
            u1 = 1.0f - u0;
        } else if (textureAspect < CONTENT_ASPECT_RATIO) {
            // El frame es más alto que el contenido -> Letterbox (barras arriba/abajo).
            float expectedContentHeight = textureDim.width / CONTENT_ASPECT_RATIO;
            float barHeight = (textureDim.height - expectedContentHeight) / 2.0f;
            v0 = barHeight / textureDim.height;
            v1 = 1.0f - v0;
        }

        // 4. Dibujo el quad usando las dimensiones calculadas y las coordenadas UV corregidas.
        RenderSystem.assertOnRenderThread();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texId);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().getBuffer();

        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(mat, x,         y + drawH, 0f).texture(u0, v1).next();
        buf.vertex(mat, x + drawW, y + drawH, 0f).texture(u1, v1).next();
        buf.vertex(mat, x + drawW, y,         0f).texture(u1, v0).next();
        buf.vertex(mat, x,         y,         0f).texture(u0, v0).next();
        Tessellator.getInstance().draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public void dispose() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            lastTexId = 0;
        }
    }
}