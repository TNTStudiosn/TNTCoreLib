// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/freeze/FreezeManager.java
package com.TNTStudios.tntcorelib.modulo.freeze;

import com.TNTStudios.tntcorelib.api.freeze.FreezeApi;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.UUID;

/**
 * Mi implementación de la API de Freeze.
 * AHORA: Las operaciones no causan lag de disco, delegan el guardado de forma asíncrona.
 */
public class FreezeManager implements FreezeApi {

    private final FreezeConfig config;

    public FreezeManager() {
        this.config = FreezeConfig.load();
    }

    @Override
    public void freezePlayer(ServerPlayerEntity player) {
        if (config.frozenPlayers.add(player.getUuid())) {
            // En lugar de guardar, solo marco que hay cambios.
            config.markDirty();
        }
    }

    @Override
    public void freezePlayer(Collection<ServerPlayerEntity> players) {
        boolean changed = false;
        for (ServerPlayerEntity player : players) {
            if (config.frozenPlayers.add(player.getUuid())) {
                changed = true;
            }
        }

        if (changed) {
            config.markDirty();
        }
    }

    @Override
    public void unfreezePlayer(ServerPlayerEntity player) {
        if (config.frozenPlayers.remove(player.getUuid())) {
            config.markDirty();
        }
    }

    @Override
    public void unfreezePlayer(Collection<ServerPlayerEntity> players) {
        boolean changed = false;
        for (ServerPlayerEntity player : players) {
            if (config.frozenPlayers.remove(player.getUuid())) {
                changed = true;
            }
        }

        if (changed) {
            config.markDirty();
        }
    }

    @Override
    public boolean isPlayerFrozen(ServerPlayerEntity player) {
        // La lectura es siempre desde la memoria, es instantánea.
        return isPlayerFrozen(player.getUuid());
    }

    @Override
    public boolean isPlayerFrozen(UUID playerUuid) {
        return config.frozenPlayers.contains(playerUuid);
    }

    /**
     * Expongo el método de guardado para que el Handler lo pueda llamar.
     * @param async Si debe ser asíncrono o no.
     */
    public void saveState(boolean async) {
        config.save(async);
    }
}