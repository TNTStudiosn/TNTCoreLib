// ruta: src/client/java/com/TNTStudios/tntcorelib/client/modulo/antitrampas/AntiTrampasOverlay.java
package com.TNTStudios.tntcorelib.client.modulo.antitrampas;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Mi clase que se encarga de renderizar el aviso visual del anti-trampas.
 * Dibuja un borde y un texto cuando el jugador no está en la ventana del juego.
 */
public class AntiTrampasOverlay {

    // Defino las constantes para el diseño para poder ajustarlas fácilmente.
    private static final int BORDER_COLOR = 0xCC8A2BE2; // Un morado semi-transparente (BlueViolet con alpha CC).
    private static final int BORDER_THICKNESS = 5; // Grosor del borde en píxeles.

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // 1. Dibujo el borde morado.
        // Lo hago dibujando cuatro rectángulos en los bordes de la pantalla.
        // Borde superior
        context.fill(0, 0, screenWidth, BORDER_THICKNESS, BORDER_COLOR);
        // Borde inferior
        context.fill(0, screenHeight - BORDER_THICKNESS, screenWidth, screenHeight, BORDER_COLOR);
        // Borde izquierdo
        context.fill(0, BORDER_THICKNESS, BORDER_THICKNESS, screenHeight - BORDER_THICKNESS, BORDER_COLOR);
        // Borde derecho
        context.fill(screenWidth - BORDER_THICKNESS, BORDER_THICKNESS, screenWidth, screenHeight - BORDER_THICKNESS, BORDER_COLOR);


        // 2. Dibujo el texto de advertencia.
        Text warningText = Text.literal("Estás en otra pantalla").formatted(Formatting.WHITE, Formatting.BOLD);
        int textWidth = client.textRenderer.getWidth(warningText);

        // Calculo las coordenadas para centrar el texto en la pantalla.
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight / 2) - (client.textRenderer.fontHeight / 2);

        // Dibujo el texto con sombra para que sea legible sobre cualquier fondo.
        context.drawTextWithShadow(client.textRenderer, warningText, x, y, 0xFFFFFF);
    }
}