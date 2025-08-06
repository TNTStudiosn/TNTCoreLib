// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/playerstats/ClientStatsManager.java
package com.TNTStudios.tntcorelib.client.modulo.playerstats;

/**
 * Mi gestor de estado para el lado cliente.
 * Almacena si la vida o el hambre del jugador están pausadas
 * para que la interfaz (HUD) pueda reaccionar y ocultar las barras.
 * La información se recibe del servidor a través de paquetes.
 */
public final class ClientStatsManager {

    // Variables estáticas para un acceso rápido y sencillo desde cualquier parte del cliente.
    private static boolean healthPaused = false;
    private static boolean foodPaused = false;

    // Hago el constructor privado porque esta es una clase de utilidad estática.
    private ClientStatsManager() {}

    /**
     * Actualiza el estado de las estadísticas del jugador en el cliente.
     * Este método lo llama mi manejador de paquetes cuando llega la información del servidor.
     * @param newHealthPaused El nuevo estado para la vida.
     * @param newFoodPaused El nuevo estado para el hambre.
     */
    public static void updateStats(boolean newHealthPaused, boolean newFoodPaused) {
        healthPaused = newHealthPaused;
        foodPaused = newFoodPaused;
    }

    /**
     * Comprueba si la vida del jugador está actualmente pausada.
     * Lo uso en mi InGameHudMixin para decidir si renderizar los corazones.
     * @return true si la vida está pausada, false en caso contrario.
     */
    public static boolean isHealthPaused() {
        return healthPaused;
    }

    /**
     * Comprueba si el hambre del jugador está actualmente pausada.
     * Lo uso en mi InGameHudMixin para decidir si renderizar la barra de hambre.
     * @return true si el hambre está pausado, false en caso contrario.
     */
    public static boolean isFoodPaused() {
        return foodPaused;
    }
}
