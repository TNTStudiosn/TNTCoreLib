// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/chatcontrol/ChatControlManager.java
package com.TNTStudios.tntcorelib.modulo.chatcontrol;

import com.TNTStudios.tntcorelib.api.chat.ChatControlApi;

/**
 * Mi implementación de la API de control de chat.
 * Centraliza la lógica para modificar el estado del chat y persistir los cambios.
 */
public class ChatControlManager implements ChatControlApi {

    private final ChatControlConfig config;

    public ChatControlManager() {
        this.config = ChatControlConfig.load();
    }

    @Override
    public void setChatMuted(boolean muted) {
        if (this.config.chatGloballyMuted != muted) {
            this.config.chatGloballyMuted = muted;
            this.config.save();
        }
    }

    @Override
    public boolean isChatMuted() {
        return this.config.chatGloballyMuted;
    }
}