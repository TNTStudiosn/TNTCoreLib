// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/freeze/FreezeApi.java
package com.TNTStudios.tntcorelib.api.freeze;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;
import java.util.UUID;

/**
 * Mi API para controlar la inmovilización de jugadores.
 * Me permite congelar o descongelar jugadores, manteniendo su estado
 * incluso si se reinicia el servidor.
 */
public interface FreezeApi {

    /**
     * Congela a un jugador específico, impidiendo su movimiento.
     * @param player El jugador a congelar.
     */
    void freezePlayer(ServerPlayerEntity player);

    /**
     * Congela a una colección de jugadores.
     * @param players Los jugadores a congelar.
     */
    void freezePlayer(Collection<ServerPlayerEntity> players);

    /**
     * Descongela a un jugador, permitiéndole moverse de nuevo.
     * @param player El jugador a descongelar.
     */
    void unfreezePlayer(ServerPlayerEntity player);

    /**
     * Descongela a una colección de jugadores.
     * @param players Los jugadores a descongelar.
     */
    void unfreezePlayer(Collection<ServerPlayerEntity> players);

    /**
     * Comprueba si un jugador está actualmente congelado.
     * @param player El jugador a comprobar.
     * @return true si el jugador está congelado, false en caso contrario.
     */
    boolean isPlayerFrozen(ServerPlayerEntity player);

    /**
     * Comprueba si un jugador está congelado usando su UUID.
     * @param playerUuid El UUID del jugador a comprobar.
     * @return true si el jugador está congelado, false en caso contrario.
     */
    boolean isPlayerFrozen(UUID playerUuid);
}