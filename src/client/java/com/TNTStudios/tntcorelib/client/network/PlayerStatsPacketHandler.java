// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/network/PlayerStatsPacketHandler.java
package com.TNTStudios.tntcorelib.client.network;

import com.TNTStudios.tntcorelib.client.modulo.playerstats.ClientStatsManager;
import com.TNTStudios.tntcorelib.network.ModPackets;
import com.TNTStudios.tntcorelib.network.packet.PlayerStatsSyncS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Mi manejador para recibir los paquetes de estadísticas en el cliente.
 * Se registra para escuchar nuestro paquete personalizado y actualiza el ClientStatsManager.
 */
public class PlayerStatsPacketHandler {

    public static void register() {
        // La firma de nuestro método 'receive' coincide con lo que el handler espera.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.PLAYER_STATS_SYNC_ID, PlayerStatsPacketHandler::receive);
    }

    /**
     * Mi método para recibir el paquete.
     * La firma ahora acepta el objeto PlayerStatsSyncS2CPacket directamente.
     * Fabric se encarga de crear el paquete desde el PacketByteBuf por nosotros.
     *
     * @param packet El paquete de datos ya deserializado.
     * @param player El jugador del lado del cliente.
     * @param sender El objeto para enviar respuestas si fuera necesario.
     */
    private static void receive(PlayerStatsSyncS2CPacket packet, ClientPlayerEntity player, PacketSender sender) {
        // Los datos ya vienen procesados en el objeto 'packet'.
        boolean healthPaused = packet.healthPaused();
        boolean foodPaused = packet.foodPaused();

        // ✅ CORREGIDO: Me aseguro de ejecutar la lógica en el hilo principal del cliente.
        // En lugar de acceder a 'player.client', que es un campo protegido,
        // uso MinecraftClient.getInstance() para obtener la instancia del cliente de forma segura.
        // Esto evita problemas de concurrencia y es la práctica estándar.
        MinecraftClient.getInstance().execute(() -> {
            // Llamo a mi ClientStatsManager para que actualice su estado.
            ClientStatsManager.updateStats(healthPaused, foodPaused);
        });
    }
}