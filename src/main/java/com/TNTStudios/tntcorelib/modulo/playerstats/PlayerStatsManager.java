// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/playerstats/PlayerStatsManager.java
package com.TNTStudios.tntcorelib.modulo.playerstats;

import com.TNTStudios.tntcorelib.api.playerstats.PlayerStatsApi;
import com.TNTStudios.tntcorelib.network.ModPackets;
import com.TNTStudios.tntcorelib.network.packet.PlayerStatsSyncS2CPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mi implementación de la API para estadísticas del jugador.
 * Gestiona quién tiene la vida o el hambre pausados y aplica los efectos.
 * AHORA: También notifica al cliente sobre los cambios de estado.
 */
public class PlayerStatsManager implements PlayerStatsApi {

    private final Set<UUID> healthPausedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> foodPausedPlayers = ConcurrentHashMap.newKeySet();

    public PlayerStatsManager() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (UUID playerUuid : foodPausedPlayers) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
                if (player != null) {
                    HungerManager hungerManager = player.getHungerManager();
                    if (hungerManager.getFoodLevel() < 20 || hungerManager.getSaturationLevel() < 5.0f) {
                        hungerManager.setFoodLevel(20);
                        hungerManager.setSaturationLevel(5.0f);
                    }
                }
            }
        });
    }

    /**
     * Mi método para sincronizar el estado del jugador con su cliente.
     * Envía un paquete con el estado actual de pausa de vida y hambre.
     * @param player El jugador al que se le enviará la actualización.
     */
    public void syncWithClient(ServerPlayerEntity player) {
        if (player == null) return;
        boolean healthPaused = isHealthPaused(player);
        boolean foodPaused = isFoodPaused(player);

        // ✅ Ahora envío el paquete real que he creado. ¡Adiós al hack!
        ServerPlayNetworking.send(player, new PlayerStatsSyncS2CPacket(healthPaused, foodPaused));
    }

    // --- IMPLEMENTACIÓN DE VIDA ---

    @Override
    public void pauseHealth(ServerPlayerEntity player) {
        healthPausedPlayers.add(player.getUuid());
        player.setInvulnerable(true);
        syncWithClient(player); // Sincronizo con el cliente
    }

    // ... los demás métodos de pauseHealth/resumeHealth deben llamar a syncWithClient ...

    @Override
    public void resumeHealth(ServerPlayerEntity player) {
        healthPausedPlayers.remove(player.getUuid());
        if (!player.getAbilities().invulnerable) {
            player.setInvulnerable(false);
        }
        syncWithClient(player); // Sincronizo con el cliente
    }

    // --- IMPLEMENTACIÓN DE HAMBRE ---

    @Override
    public void pauseFood(ServerPlayerEntity player) {
        foodPausedPlayers.add(player.getUuid());
        syncWithClient(player); // Sincronizo con el cliente
    }

    // ... los demás métodos de pauseFood/resumeFood deben llamar a syncWithClient ...

    @Override
    public void resumeFood(ServerPlayerEntity player) {
        foodPausedPlayers.remove(player.getUuid());
        syncWithClient(player); // Sincronizo con el cliente
    }

    // --- Los demás métodos no necesitan cambios ---
    // (setHealth, setFoodLevel, isHealthPaused, isFoodPaused, etc.)

    // Aquí el resto de tu clase PlayerStatsManager...
    @Override
    public void pauseHealth(Collection<ServerPlayerEntity> players) {
        players.forEach(this::pauseHealth);
    }

    @Override
    public void resumeHealth(Collection<ServerPlayerEntity> players) {
        players.forEach(this::resumeHealth);
    }

    @Override
    public void setHealth(ServerPlayerEntity player, float health) {
        player.setHealth(health);
    }

    @Override
    public void setHealth(Collection<ServerPlayerEntity> players, float health) {
        players.forEach(p -> setHealth(p, health));
    }

    @Override
    public void pauseFood(Collection<ServerPlayerEntity> players) {
        players.forEach(this::pauseFood);
    }

    @Override
    public void resumeFood(Collection<ServerPlayerEntity> players) {
        players.forEach(this::resumeFood);
    }

    @Override
    public void setFoodLevel(ServerPlayerEntity player, int foodLevel) {
        player.getHungerManager().setFoodLevel(foodLevel);
    }

    @Override
    public void setFoodLevel(Collection<ServerPlayerEntity> players, int foodLevel) {
        players.forEach(p -> setFoodLevel(p, foodLevel));
    }

    @Override
    public boolean isHealthPaused(ServerPlayerEntity player) {
        return healthPausedPlayers.contains(player.getUuid());
    }

    @Override
    public boolean isFoodPaused(ServerPlayerEntity player) {
        return foodPausedPlayers.contains(player.getUuid());
    }
}