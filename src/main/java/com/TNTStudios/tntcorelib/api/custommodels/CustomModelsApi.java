// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/custommodels/CustomModelsApi.java
package com.TNTStudios.tntcorelib.api.custommodels;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;
import java.util.List;

/**
 * Mi API para gestionar los modelos personalizados de los jugadores.
 * Permite a otros mods establecer, resetear y consultar los modelos disponibles
 * de una manera segura y desacoplada de la implementación interna.
 */
public interface CustomModelsApi {

    /**
     * Aplica un modelo personalizado a un jugador específico.
     * El cliente del jugador recibirá un paquete para ejecutar el comando de CPM.
     *
     * @param player El jugador al que se le aplicará el modelo.
     * @param modelName El nombre del archivo del modelo (sin la extensión .cpmmodel).
     */
    void setModel(ServerPlayerEntity player, String modelName);

    /**
     * Aplica un modelo personalizado a una colección de jugadores.
     *
     * @param players La colección de jugadores.
     * @param modelName El nombre del modelo a aplicar.
     */
    void setModel(Collection<ServerPlayerEntity> players, String modelName);

    /**
     * Restablece el modelo del jugador a su estado por defecto.
     *
     * @param player El jugador cuyo modelo se restablecerá.
     */
    void resetModel(ServerPlayerEntity player);

    /**
     * Restablece el modelo de una colección de jugadores a su estado por defecto.
     *
     * @param players La colección de jugadores.
     */
    void resetModel(Collection<ServerPlayerEntity> players);

    /**
     * Obtiene una lista con los nombres de todos los modelos disponibles en la carpeta de configuración.
     *
     * @return Una lista de strings con los nombres de los modelos (sin extensión).
     */
    List<String> getAvailableModels();
}