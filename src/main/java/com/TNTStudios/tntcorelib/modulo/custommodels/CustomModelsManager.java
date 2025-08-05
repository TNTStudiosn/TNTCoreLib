package com.TNTStudios.tntcorelib.modulo.custommodels;

import com.TNTStudios.tntcorelib.api.custommodels.CustomModelsApi;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes; // ✅ NUEVO: Importo el tipo de partícula que necesito.
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld; // ✅ NUEVO: Importo ServerWorld para poder generar partículas.
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mi implementación de la API de modelos personalizados.
 * Esta clase centraliza toda la lógica del servidor, incluyendo los efectos visuales.
 */
public class CustomModelsManager implements CustomModelsApi {

    private final MinecraftServer server;
    private final CustomModelsConfig config;
    private final File modelFolder;

    public static final Identifier SET_MODEL_PACKET_ID = new Identifier("tntcorelib", "set_model");
    public static final Identifier RESET_MODEL_PACKET_ID = new Identifier("tntcorelib", "reset_model");

    public CustomModelsManager(MinecraftServer server, CustomModelsConfig config) {
        this.server = server;
        this.config = config;
        this.modelFolder = new File(server.getRunDirectory(), config.modelFolderName);
        if (!modelFolder.exists()) {
            modelFolder.mkdirs();
        }
    }

    /**
     * ✅ NUEVO: Mi método para generar las partículas de transformación.
     * Lo he creado para reutilizar el código y mantener todo más limpio.
     * @param player El jugador alrededor del cual aparecerán las partículas.
     */
    private void triggerTransformationEffect(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        if (world != null) {
            // Parámetros del comando: /particle minecraft:flash ~ ~1 ~ 0.5 0.5 0.5 2 200 normal
            world.spawnParticles(
                    ParticleTypes.FLASH,
                    player.getX(),
                    player.getY() + 1.0, // Un bloque por encima de los pies del jugador.
                    player.getZ(),
                    200,    // count
                    0.5,    // deltaX
                    0.5,    // deltaY
                    0.5,    // deltaZ
                    0.0     // speed (el comando original usa 2, pero para FLASH, el speed no tiene un efecto notable y puede dejarse en 0)
            );
        }
    }

    @Override
    public void setModel(ServerPlayerEntity player, String modelName) {
        // Primero aplico el efecto y luego envío el paquete de red.
        triggerTransformationEffect(player);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(modelName);
        ServerPlayNetworking.send(player, SET_MODEL_PACKET_ID, buf);
    }

    @Override
    public void setModel(Collection<ServerPlayerEntity> players, String modelName) {
        for (ServerPlayerEntity player : players) {
            setModel(player, modelName);
        }
    }

    @Override
    public void resetModel(ServerPlayerEntity player) {
        // Igual que al establecer, aplico el efecto visual.
        triggerTransformationEffect(player);

        PacketByteBuf buf = PacketByteBufs.empty();
        ServerPlayNetworking.send(player, RESET_MODEL_PACKET_ID, buf);
    }

    @Override
    public void resetModel(Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            resetModel(player);
        }
    }

    @Override
    public List<String> getAvailableModels() {
        if (!modelFolder.exists() || !modelFolder.isDirectory()) {
            return Collections.emptyList();
        }
        File[] modelFiles = modelFolder.listFiles((dir, name) -> name.endsWith(".cpmmodel"));
        if (modelFiles == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(modelFiles)
                .map(file -> file.getName().replace(".cpmmodel", ""))
                .collect(Collectors.toList());
    }
}