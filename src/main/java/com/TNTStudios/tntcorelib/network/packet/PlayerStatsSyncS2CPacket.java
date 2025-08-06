// Ubicación: src/main/java/com/TNTStudios/tntcorelib/network/packet/PlayerStatsSyncS2CPacket.java
package com.TNTStudios.tntcorelib.network.packet;

import com.TNTStudios.tntcorelib.network.ModPackets;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

/**
 * Mi paquete para sincronizar el estado de las estadísticas del jugador desde el servidor al cliente.
 * Contiene dos booleanos simples para indicar si la vida y/o el hambre están pausados.
 */
public record PlayerStatsSyncS2CPacket(boolean healthPaused, boolean foodPaused) implements FabricPacket {

    // ✅ NUEVO: Constructor para leer desde el buffer.
    // Esto permite que la referencia PlayerStatsSyncS2CPacket::new funcione en ModPackets.
    public PlayerStatsSyncS2CPacket(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean());
    }

    // Escribo los datos en el buffer para su envío.
    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(healthPaused);
        buf.writeBoolean(foodPaused);
    }

    // Devuelvo el tipo de paquete, que contiene nuestro identificador único.
    @Override
    public PacketType<?> getType() {
        return ModPackets.PLAYER_STATS_SYNC_ID;
    }
}