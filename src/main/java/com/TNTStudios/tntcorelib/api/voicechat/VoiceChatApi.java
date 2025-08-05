// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/voicechat/VoiceChatApi.java
package com.TNTStudios.tntcorelib.api.voicechat;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;

/**
 * Mi API para gestionar el estado de muteo de los jugadores en PlasmoVoice.
 * Permite silenciar o desilenciar a jugadores de forma individual o a todos a la vez,
 * desacoplando la lógica de la implementación del comando.
 */
public interface VoiceChatApi {

    /**
     * Silencia la voz de un jugador específico en el servidor.
     *
     * @param player El jugador que quiero silenciar.
     */
    void mutePlayer(ServerPlayerEntity player);

    /**
     * Permite que un jugador específico vuelva a usar el chat de voz.
     *
     * @param player El jugador al que le quiero devolver la voz.
     */
    void unmutePlayer(ServerPlayerEntity player);

    /**
     * Silencia a todos los jugadores conectados en el servidor.
     * Es ideal para momentos en los que necesito silencio absoluto en un evento.
     */
    void muteAllPlayers();

    /**
     * Devuelve la voz a todos los jugadores que estaban silenciados.
     */
    void unmuteAllPlayers();
}