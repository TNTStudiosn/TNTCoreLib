// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/playerstats/PlayerStatsApi.java
package com.TNTStudios.tntcorelib.api.playerstats;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;

/**
 * Mi API para controlar el estado de la vida y el hambre de los jugadores.
 * Me permite pausar/reanudar y establecer valores específicos de forma individual o global.
 */
public interface PlayerStatsApi {

    // --- MÉTODOS DE VIDA ---

    /**
     * Pausa la vida de un jugador. No recibirá daño y sus corazones se ocultarán.
     * @param player El jugador cuya vida se pausará.
     */
    void pauseHealth(ServerPlayerEntity player);

    /**
     * Pausa la vida para una colección de jugadores.
     * @param players Los jugadores a afectar.
     */
    void pauseHealth(Collection<ServerPlayerEntity> players);

    /**
     * Reanuda la vida de un jugador, volviendo a su estado normal.
     * @param player El jugador cuya vida se reanudará.
     */
    void resumeHealth(ServerPlayerEntity player);

    /**
     * Reanuda la vida para una colección de jugadores.
     * @param players Los jugadores a afectar.
     */
    void resumeHealth(Collection<ServerPlayerEntity> players);

    /**
     * Establece la vida de un jugador a un valor específico.
     * @param player El jugador a afectar.
     * @param health La cantidad de vida (1.0f = medio corazón).
     */
    void setHealth(ServerPlayerEntity player, float health);

    /**
     * Establece la vida para una colección de jugadores.
     * @param players Los jugadores a afectar.
     * @param health La cantidad de vida.
     */
    void setHealth(Collection<ServerPlayerEntity> players, float health);

    // --- MÉTODOS DE HAMBRE ---

    /**
     * Pausa el hambre de un jugador. No se consumirá y la barra se ocultará.
     * @param player El jugador cuya hambre se pausará.
     */
    void pauseFood(ServerPlayerEntity player);

    /**
     * Pausa el hambre para una colección de jugadores.
     * @param players Los jugadores a afectar.
     */
    void pauseFood(Collection<ServerPlayerEntity> players);

    /**
     * Reanuda el hambre de un jugador, volviendo a su estado normal.
     * @param player El jugador cuya hambre se reanudará.
     */
    void resumeFood(ServerPlayerEntity player);

    /**
     * Reanuda el hambre para una colección de jugadores.
     * @param players Los jugadores a afectar.
     */
    void resumeFood(Collection<ServerPlayerEntity> players);

    /**
     * Establece el nivel de hambre de un jugador.
     * @param player El jugador a afectar.
     * @param foodLevel El nivel de hambre (20 = barra llena).
     */
    void setFoodLevel(ServerPlayerEntity player, int foodLevel);

    /**
     * Establece el nivel de hambre para una colección de jugadores.
     * @param players Los jugadores a afectar.
     * @param foodLevel El nivel de hambre.
     */
    void setFoodLevel(Collection<ServerPlayerEntity> players, int foodLevel);

    /**
     * Comprueba si la vida de un jugador está pausada.
     * @param player El jugador a comprobar.
     * @return true si la vida está pausada, false en caso contrario.
     */
    boolean isHealthPaused(ServerPlayerEntity player);

    /**
     * Comprueba si el hambre de un jugador está pausada.
     * @param player El jugador a comprobar.
     * @return true si el hambre está pausado, false en caso contrario.
     */
    boolean isFoodPaused(ServerPlayerEntity player);
}