// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/LoadScreen.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Mi clase para la pantalla de carga, ahora con una transición de entrada
 * suave y una animación de carga mejorada.
 * Se encarga de gestionar el video de fondo y los elementos visuales.
 */
public final class LoadScreen {
    private static VideoBackgroundManager videoManager;

    // Constantes para la animación de carga.
    private static final String MOJANG_DISCLAIMER = "No afiliado a Mojang Studios.";
    private static final String[] LOADING_ANIMATION_FRAMES = {"|", "/", "-", "\\"};
    private static final int ANIMATION_FRAME_DURATION_MS = 150;
    private static long lastAnimationUpdateTime = 0;
    private static int animationFrameIndex = 0;

    // Variables para la transición de opacidad (fade-in).
    private static final long FADE_IN_DURATION_MS = 1500;
    private static long fadeInStartTime = -1;
    private static float currentAlpha = 0.0f;

    private static final long TEXT_VISIBILITY_DELAY_MS = 2600;


    private LoadScreen() {}

    public static void tryInitVideo() {
        if (videoManager == null) {
            videoManager = new VideoBackgroundManager(CustomMenuHandler.LOAD_VIDEO_FILE, false);
            if (videoManager.initialize()) {
                // Empiezo el contador para el fade-in justo después de inicializar el video.
                // Este tiempo de inicio también me servirá para el retraso del texto.
                fadeInStartTime = System.currentTimeMillis();
            } else {
                videoManager = null;
            }
        }
    }

    public static void render(DrawContext ctx, int screenW, int screenH) {
        // Primero, actualizo el estado de la animación y la opacidad.
        updateState();

        // Si la opacidad es casi cero, no tiene sentido renderizar nada.
        if (currentAlpha <= 0.01f) {
            return;
        }

        // Aplico la opacidad globalmente.
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, currentAlpha);

        if (videoManager != null) {
            videoManager.render(ctx, screenW, screenH);
        }

        // ✅ NUEVO: Solo renderizo el texto si ha pasado el tiempo de retraso definido.
        // Calculo el tiempo que ha pasado desde que la pantalla de carga se inició.
        long elapsedTime = System.currentTimeMillis() - fadeInStartTime;

        // Si el tiempo transcurrido es mayor o igual al retraso, muestro los textos.
        if (fadeInStartTime > 0 && elapsedTime >= TEXT_VISIBILITY_DELAY_MS) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Convierto el color base (blanco) a un entero con el canal alfa ya aplicado.
            int textColor = ((int)(currentAlpha * 255) << 24) | 0xFFFFFF;

            // Renderizo la leyenda en la esquina superior izquierda.
            ctx.drawTextWithShadow(
                    client.textRenderer,
                    MOJANG_DISCLAIMER,
                    5,
                    5,
                    textColor
            );

            // Construyo el texto de carga con el frame de animación actual.
            String loadingText = "Cargando " + LOADING_ANIMATION_FRAMES[animationFrameIndex];

            // Calculo la posición para que el texto quede en la esquina inferior derecha.
            int textWidth = client.textRenderer.getWidth(loadingText);
            int x = screenW - textWidth - 5;
            int y = screenH - client.textRenderer.fontHeight - 5;

            ctx.drawTextWithShadow(
                    client.textRenderer,
                    loadingText,
                    x,
                    y,
                    textColor
            );
        }

        // Restauro el color del shader para no afectar otros renderizados.
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    /**
     * Método para actualizar la opacidad y la animación.
     * Centralizo aquí toda la lógica de estado que depende del tiempo.
     */
    private static void updateState() {
        long currentTime = System.currentTimeMillis();

        // Lógica de la transición de opacidad (fade-in).
        if (fadeInStartTime > 0) {
            long elapsedTime = currentTime - fadeInStartTime;
            if (elapsedTime < FADE_IN_DURATION_MS) {
                currentAlpha = (float) elapsedTime / FADE_IN_DURATION_MS;
            } else {
                currentAlpha = 1.0f;
                // No reseteo fadeInStartTime aquí, porque lo sigo necesitando para el retraso del texto.
            }
        }

        // Lógica de la animación de carga.
        if (currentTime - lastAnimationUpdateTime > ANIMATION_FRAME_DURATION_MS) {
            lastAnimationUpdateTime = currentTime;
            animationFrameIndex = (animationFrameIndex + 1) % LOADING_ANIMATION_FRAMES.length;
        }
    }

    public static void stopVideo() {
        if (videoManager != null) {
            videoManager.dispose();
            videoManager = null;
        }
        // Reseteo los valores para que la pantalla de carga funcione
        // correctamente si se vuelve a mostrar.
        resetState();
    }

    /**
     * Método para resetear el estado de la pantalla.
     */
    private static void resetState() {
        fadeInStartTime = -1;
        currentAlpha = 0.0f;
        animationFrameIndex = 0;
    }
}