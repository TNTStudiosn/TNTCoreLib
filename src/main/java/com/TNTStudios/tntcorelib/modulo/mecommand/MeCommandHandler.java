// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/mecommand/MeCommandHandler.java
package com.TNTStudios.tntcorelib.modulo.mecommand;

/**
 * Mi manejador para el módulo del comando /me.
 * Su única responsabilidad es cargar y mantener la configuración,
 * que será utilizada por el MeCommandMixin para decidir si el comando
 * vanilla debe ser registrado o no.
 */
public class MeCommandHandler {

    private static MeCommandConfig config;

    /**
     * Inicializo el módulo cargando la configuración desde el archivo.
     * Esto debe llamarse una vez al iniciar el mod.
     */
    public static void init() {
        config = MeCommandConfig.load();
        // Ya no registramos ningún comando desde aquí. El mixin se encarga de todo.
    }

    /**
     * Proporciona acceso a la configuración cargada.
     * Es crucial para que el mixin pueda leer el estado de 'commandEnabled'.
     * @return La instancia de la configuración del comando /me.
     */
    public static MeCommandConfig getConfig() {
        return config;
    }
}