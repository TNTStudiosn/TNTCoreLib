package com.TNTStudios.tntcorelib.client.modulo.tablist;

/**
 * Mi manejador para el módulo del Tablist personalizado.
 * Ahora se encarga de registrar el comando de prueba.
 */
public class TablistHandler {

    public static void init() {
        // Registro mi comando de cliente para poder testear el tablist.
        TablistTestCommand.register();
    }
}