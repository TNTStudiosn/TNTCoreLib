// Ubicación: src/main/java/com/TNTStudios/tntcorelib/network/ModPackets.java
package com.TNTStudios.tntcorelib.network;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.network.packet.PlayerStatsSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Mi clase para registrar todos los paquetes de red del mod.
 * Así mantengo todo organizado.
 */
public class ModPackets {

    // Defino un identificador único para mi paquete de sincronización.
    public static final PacketType<PlayerStatsSyncS2CPacket> PLAYER_STATS_SYNC_ID =
            create("player_stats_sync", PlayerStatsSyncS2CPacket::new);

    // Un método ayudante para crear los PacketType de forma limpia.
    private static <T extends PlayerStatsSyncS2CPacket> PacketType<T> create(String path, Function<PacketByteBuf, T> reader) {
        return PacketType.create(new Identifier(Tntcorelib.MOD_ID, path), reader);
    }

    // Este método podría llamarse desde la clase principal del mod para asegurar que todo se cargue.
    public static void register() {
        // La inicialización estática ya hace el trabajo, pero es buena práctica tener un método de registro.
        Tntcorelib.LOGGER.info("Registrando paquetes para " + Tntcorelib.MOD_ID);
    }
}