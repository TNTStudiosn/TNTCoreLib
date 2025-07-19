package com.TNTStudios.tntcorelib.api.tablist;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.UUID;

/**
 * Mi API para controlar la visibilidad de los jugadores en el Tablist del servidor.
 * Con estos métodos, puedo ocultar o mostrar jugadores de forma selectiva
 * para otros jugadores, lo que me será muy útil para sistemas de vanish o eventos.
 * La interacción se hace siempre a través de esta interfaz para mantener el código limpio.
 */
public interface TablistApi {

    /**
     * Oculta un jugador específico de la lista de otro jugador (el observador).
     * Esto enviará un paquete al observador para que elimine al jugador de su Tablist.
     *
     * @param playerToHide El jugador que quiero ocultar.
     * @param observer El jugador que dejará de ver a playerToHide.
     */
    void hidePlayer(ServerPlayerEntity playerToHide, ServerPlayerEntity observer);

    /**
     * Muestra un jugador que previamente estaba oculto para un observador.
     * Esto enviará un paquete para que el jugador vuelva a aparecer en el Tablist del observador.
     *
     * @param playerToShow El jugador que quiero volver a mostrar.
     * @param observer El jugador que volverá a ver a playerToShow.
     */
    void showPlayer(ServerPlayerEntity playerToShow, ServerPlayerEntity observer);

    /**
     * Comprueba si un jugador está actualmente oculto para un observador específico.
     *
     * @param player The player to check visibility for.
     * @param observer El jugador desde cuya perspectiva hago la comprobación.
     * @return true si el jugador está oculto para el observador, false en caso contrario.
     */
    boolean isPlayerHidden(ServerPlayerEntity player, ServerPlayerEntity observer);

    /**
     * Comprueba si un jugador está actualmente oculto para un observador específico, usando sus UUIDs.
     *
     * @param playerUuid El UUID del jugador a comprobar.
     * @param observerUuid El UUID del observador.
     * @return true si el jugador está oculto para el observador, false en caso contrario.
     */
    boolean isPlayerHidden(UUID playerUuid, UUID observerUuid);
}