// src/main/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/FadeOverlay.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.MathHelper;

/**
 * Dibuja un overlay negro que hace fade-in (0→1) y luego fade-out (1→0) en toda la pantalla.
 */
public class FadeOverlay {
    // Duración total del efecto (ms)
    private final long durationMs;

    // Tiempo de inicio
    private long startTime = -1;

    public FadeOverlay(long durationMs) {
        this.durationMs = durationMs;
    }

    /** Arranca el fade (se llama justo cuando termina el video). */
    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    /** Devuelve true mientras el fade siga activo. */
    public boolean isActive() {
        if (startTime < 0) return false;
        return System.currentTimeMillis() - startTime < durationMs;
    }

    /**
     * Renderiza el overlay negro con alpha:
     *   t en [0,0.5] → alpha = smoothstep(t*2)   (fade-in)
     *   t en (0.5,1] → alpha = 1 - smoothstep((t-0.5)*2) (fade-out)
     */
    public void render(DrawContext ctx, int screenW, int screenH) {
        if (!isActive()) return;

        float t = MathHelper.clamp((System.currentTimeMillis() - startTime) / (float) durationMs, 0f, 1f);
        float alpha;
        if (t <= 0.5f) {
            float inT = t * 2f;
            alpha = smooth(inT);
        } else {
            float outT = (t - 0.5f) * 2f;
            alpha = 1f - smooth(outT);
        }

        int a = MathHelper.ceil(alpha * 255f);
        int color = Argb.getArgb(a, 0, 0, 0);

        // Render full-screen rect
        ctx.fill(RenderLayer.getGuiOverlay(), 0, 0, screenW, screenH, color);
    }

    // Cubic smoothstep:  t*t*(3 − 2*t)
    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }
}
