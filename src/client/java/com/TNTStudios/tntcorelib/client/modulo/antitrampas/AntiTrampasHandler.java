// ruta: src/client/java/com/TNTStudios/tntcorelib/client/modulo/antitrampas/AntiTrampasHandler.java
package com.TNTStudios.tntcorelib.client.modulo.antitrampas;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

/**
 * Mi manejador para el módulo Anti-Trampas.
 * Su única responsabilidad es registrar el overlay para que se dibuje cuando sea necesario.
 */
public class AntiTrampasHandler {

    public static void init() {
        // Registro mi overlay en el evento del HUD.
        // Se ejecutará en cada frame después de que el HUD del juego se haya renderizado.
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // Me aseguro de que el jugador esté en un mundo y que la ventana del juego no esté en foco.
            // Esta es la condición clave para activar el anti-trampas.
            if (client.world != null && client.player != null && !client.isWindowFocused()) {
                AntiTrampasOverlay.render(drawContext);
            }
        });
    }
}