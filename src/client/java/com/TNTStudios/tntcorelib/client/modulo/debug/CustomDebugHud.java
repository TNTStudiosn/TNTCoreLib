package com.TNTStudios.tntcorelib.client.modulo.debug;

import com.TNTStudios.tntcorelib.client.modulo.debug.DebugHudHandler; // <- Me aseguro de importar la clase del handler.
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mi clase para renderizar el nuevo y flamante HUD de depuración (F3).
 * Reemplaza por completo el diseño de Minecraft por uno más limpio, moderno y animado.
 */
public class CustomDebugHud {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    // Controlo el estado de la animación.
    private static long animationStartTime = 0;
    private static boolean isOpen = false;
    private static final int ANIMATION_DURATION_MS = 250; // Una animación un poco más rápida.

    public static void onClose() {
        // Si se cierra, marco el estado y reseteo el tiempo.
        if (isOpen) {
            isOpen = false;
            animationStartTime = 0;
        }
    }

    public static void render(DrawContext context) {
        // Si se acaba de abrir, inicio el temporizador.
        if (!isOpen) {
            isOpen = true;
            animationStartTime = System.currentTimeMillis();
        }

        // Calculo el progreso de la animación usando una función de easing para suavizarla.
        float progress = animationStartTime == 0 ? 1.0f : Math.min((float) (System.currentTimeMillis() - animationStartTime) / ANIMATION_DURATION_MS, 1.0f);
        float easedProgress = 1 - (float) Math.pow(1 - progress, 4); // EaseOutQuart

        // Preparo los textos que voy a mostrar.
        List<String> leftText = getLeftText();
        List<String> rightText = getRightText();

        // Renderizo los dos paneles con la animación.
        renderPanel(context, leftText, true, easedProgress);
        renderPanel(context, rightText, false, easedProgress);
    }

    private static void renderPanel(DrawContext context, List<String> text, boolean isLeft, float progress) {
        TextRenderer textRenderer = client.textRenderer;
        int panelPadding = 6;
        int lineHeight = textRenderer.fontHeight + 2;
        int screenWidth = context.getScaledWindowWidth();

        // Calculo el ancho del panel dinámicamente.
        int panelWidth = 0;
        for (String s : text) {
            panelWidth = Math.max(panelWidth, textRenderer.getWidth(s));
        }
        panelWidth += panelPadding * 2;

        int panelHeight = text.size() * lineHeight + panelPadding * 2 - 2;

        // Animo la posición X para que los paneles se deslicen desde los bordes.
        int startX = isLeft ? -panelWidth : screenWidth;
        int finalX = isLeft ? 2 : screenWidth - panelWidth - 2;
        int currentX = (int) MathHelper.lerp(progress, startX, finalX);
        int y = 2;

        // Dibujo el fondo del panel con un toque moderno.
        drawRoundedRect(context, currentX, y, currentX + panelWidth, y + panelHeight, 5, 0xDD1A1A1A);

        // Renderizo cada línea con una animación de cascada.
        for (int i = 0; i < text.size(); i++) {
            // Cada línea tiene su propio progreso de animación, creando el efecto cascada.
            float lineProgress = MathHelper.clamp((progress - (i * 0.04f)) / 0.8f, 0, 1);
            if (lineProgress <= 0) continue; // No dibujo la línea hasta que le toque.

            String line = text.get(i);
            int textColor = line.startsWith("§") ? 0xFFFFFF : 0xE0E0E0; // Blanco para títulos, gris claro para datos.

            // Animo la opacidad y una pequeña traslación vertical para cada línea.
            int alpha = (int) (lineProgress * 255);
            int animatedTextColor = (alpha << 24) | textColor;
            float yOffset = (1 - lineProgress) * -5;

            int lineX = currentX + panelPadding;
            int lineY = (int) (y + panelPadding + i * lineHeight + yOffset);

            context.drawTextWithShadow(textRenderer, line, lineX, lineY, animatedTextColor);
        }
    }

    // --- Recopilación de Datos ---

    private static List<String> getLeftText() {
        List<String> list = new ArrayList<>();

        // Tomo el texto de la marca desde mi archivo de configuración.
        String brandText = DebugHudHandler.debugHudConfig.brandText;

        list.add(Formatting.AQUA + "" + Formatting.BOLD + "Detalles");
        list.add("  " + brandText); // Muestro el texto configurable.
        // list.add(String.format(Locale.ROOT, "  XYZ: %.2f, %.2f, %.2f", client.player.getX(), client.player.getY(), client.player.getZ()));
        BlockPos pos = client.player.getBlockPos();
        list.add(String.format(Locale.ROOT, "  XYZ: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ()));
        list.add("");
        list.add(Formatting.AQUA + "" + Formatting.BOLD + "Servidor");

        // Verifico si estoy en un servidor dedicado o en modo un jugador.
        if (!client.isIntegratedServerRunning() && client.getNetworkHandler() != null) {
            list.add("  Ping: " + getPlayerPing() + " ms");
            // Añado el texto personalizado que quería para el servidor.
            list.add(Formatting.GRAY + "  Servidor hosteado en " + Formatting.GOLD + "HolyHosting");
            list.add(Formatting.GRAY + "  Desarrollado por " + Formatting.AQUA + "TNTStudios");
        } else {
            list.add("  Jugando en modo un jugador");
        }

        return list;
    }

    private static List<String> getRightText() {
        List<String> list = new ArrayList<>();

        long maxMem = Runtime.getRuntime().maxMemory();
        long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        list.add(Formatting.YELLOW + "" + Formatting.BOLD + "Sistema");
        list.add(String.format(Locale.ROOT, "  CPU: %s", GlDebugInfo.getCpuInfo()));
        list.add(String.format(Locale.ROOT, "  Java: %s %dbit", System.getProperty("java.version"), client.is64Bit() ? 64 : 32));
        list.add(String.format(Locale.ROOT, "  Mem: %d%% %d/%dMB", usedMem * 100L / maxMem, usedMem / 1024L / 1024L, maxMem / 1024L / 1024L));
        list.add("");
        list.add(Formatting.YELLOW + "" + Formatting.BOLD + "Gráficos");

        // Muestro los FPS y el estado de VSync como se pidió.
        list.add("  " + client.fpsDebugString.split(" ")[0] + " FPS " + (client.options.getEnableVsync().getValue() ? "(VSync)" : ""));
        list.add("  " + GlDebugInfo.getRenderer());

        return list;
    }

    private static int getPlayerPing() {
        if (client.getNetworkHandler() != null && client.player != null && client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()) != null) {
            return client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()).getLatency();
        }
        return 0; // En singleplayer o si no se puede obtener.
    }

    /**
     * Mi nueva y mejorada función para dibujar rectángulos con esquinas redondeadas.
     * Es más simple y eficiente que la anterior.
     */
    private static void drawRoundedRect(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        // Relleno central (más eficiente que un TRIANGLE_FAN gigante)
        context.fill(x1 + radius, y1 + radius, x2 - radius, y2 - radius, color); // Centro
        context.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);     // Izquierda
        context.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);     // Derecha
        context.fill(x1 + radius, y1, x2 - radius, y1 + radius, color);     // Arriba
        context.fill(x1 + radius, y2 - radius, x2 - radius, y2, color);     // Abajo

        // Esquinas (12 segmentos es suficiente para un radio pequeño)
        drawCorner(bufferBuilder, matrix, x1 + radius, y1 + radius, radius, 180, g, h, k, f); // Arriba-Izquierda
        drawCorner(bufferBuilder, matrix, x2 - radius, y1 + radius, radius, 270, g, h, k, f); // Arriba-Derecha
        drawCorner(bufferBuilder, matrix, x2 - radius, y2 - radius, radius, 0,   g, h, k, f); // Abajo-Derecha
        drawCorner(bufferBuilder, matrix, x1 + radius, y2 - radius, radius, 90,  g, h, k, f); // Abajo-Izquierda

        RenderSystem.disableBlend();
    }

    private static void drawCorner(BufferBuilder bufferBuilder, Matrix4f matrix, int x, int y, int radius, int startAngle, float r, float g, float b, float a) {
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0).color(r, g, b, a).next();
        for (int i = 0; i <= 12; i++) {
            float angle = (startAngle + i * 90f / 12f) * (float) (Math.PI / 180.0);
            bufferBuilder.vertex(matrix, x + MathHelper.sin(angle) * radius, y - MathHelper.cos(angle) * radius, 0).color(r, g, b, a).next();
        }
        Tessellator.getInstance().draw();
    }
}