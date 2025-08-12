// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/chat/ChatControlApi.java
package com.TNTStudios.tntcorelib.api.chat;

/**
 * Mi API para controlar el estado del chat global.
 * Permite a otros módulos activar o desactivar el chat, así como consultar su estado actual,
 * de manera desacoplada de la implementación interna.
 */
public interface ChatControlApi {

    /**
     * Activa o desactiva el chat global para los jugadores sin permisos elevados.
     * @param muted true para desactivar el chat, false para activarlo.
     */
    void setChatMuted(boolean muted);

    /**
     * Comprueba si el chat global está actualmente desactivado.
     * @return true si el chat está desactivado, false en caso contrario.
     */
    boolean isChatMuted();
}