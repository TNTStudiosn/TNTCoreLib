package com.TNTStudios.tntcorelib.modulo.tablist;

import com.TNTStudios.tntcorelib.api.tablist.TablistApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mi implementación interna de la API del Tablist.
 * Esta clase contiene toda la maquinaria pesada: gestiona el estado de los jugadores
 * ocultos y se encarga de enviar los paquetes correctos a los clientes.
 * Usar un ConcurrentHashMap me garantiza la seguridad en un entorno multihilo como un servidor.
 */
public class TablistManager implements TablistApi {

    // Guardo el estado en un mapa. La clave es el UUID del observador y el valor es un conjunto
    // con los UUIDs de los jugadores que NO puede ver.
    private final Map<UUID, Set<UUID>> hiddenPlayersMap = new ConcurrentHashMap<>();
    private final MinecraftServer server;

    public TablistManager(MinecraftServer server) {
        this.server = server;
        this.registerEventHandler();
    }

    @Override
    public void hidePlayer(ServerPlayerEntity playerToHide, ServerPlayerEntity observer) {
        // Añado al jugador a la lista de ocultos del observador.
        hiddenPlayersMap.computeIfAbsent(observer.getUuid(), k -> ConcurrentHashMap.newKeySet()).add(playerToHide.getUuid());

        // Envío el paquete para eliminar al jugador del Tablist del observador.
        // La acción REMOVE_PLAYER hace exactamente lo que necesito.
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, playerToHide));
    }

    @Override
    public void showPlayer(ServerPlayerEntity playerToShow, ServerPlayerEntity observer) {
        // Elimino al jugador de la lista de ocultos del observador.
        hiddenPlayersMap.computeIfPresent(observer.getUuid(), (k, v) -> {
            v.remove(playerToShow.getUuid());
            return v;
        });

        // Envío el paquete para volver a añadir al jugador al Tablist.
        // La acción ADD_PLAYER lo reincorpora.
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, playerToShow));
    }

    @Override
    public boolean isPlayerHidden(ServerPlayerEntity player, ServerPlayerEntity observer) {
        return isPlayerHidden(player.getUuid(), observer.getUuid());
    }

    @Override
    public boolean isPlayerHidden(UUID playerUuid, UUID observerUuid) {
        Set<UUID> hiddenSet = hiddenPlayersMap.get(observerUuid);
        return hiddenSet != null && hiddenSet.contains(playerUuid);
    }

    /**
     * Registro un evento para cuando un jugador entra al servidor.
     * Es crucial para que, si un jugador entra, no vea a los que ya estaban ocultos.
     */
    private void registerEventHandler() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity newPlayer = handler.getPlayer();

            // Reviso todos los jugadores en línea.
            for (ServerPlayerEntity onlinePlayer : this.server.getPlayerManager().getPlayerList()) {
                // Si el jugador 'onlinePlayer' debe estar oculto para el 'newPlayer', le envío el paquete.
                if (isPlayerHidden(onlinePlayer.getUuid(), newPlayer.getUuid())) {
                    newPlayer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, onlinePlayer));
                }

                // Si el 'newPlayer' debe estar oculto para el 'onlinePlayer', le envío el paquete a este último.
                if (isPlayerHidden(newPlayer.getUuid(), onlinePlayer.getUuid())) {
                    onlinePlayer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, newPlayer));
                }
            }
        });
    }
}