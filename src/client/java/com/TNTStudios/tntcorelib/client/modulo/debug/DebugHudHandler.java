// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/debug/DebugHudHandler.java
package com.TNTStudios.tntcorelib.client.modulo.debug;

/**
 * Mi manejador para el módulo del F3 personalizado.
 * Su única tarea es cargar la configuración al iniciar el cliente.
 */
public class DebugHudHandler {

    public static DebugHudConfig debugHudConfig;

    public static void init() {
        // Cargo mi configuración personalizada para el HUD.
        debugHudConfig = DebugHudConfig.load();
    }
}