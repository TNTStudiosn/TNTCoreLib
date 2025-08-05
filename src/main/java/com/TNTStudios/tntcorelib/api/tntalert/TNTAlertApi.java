// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/tntalert/TNTAlertApi.java
package com.TNTStudios.tntcorelib.api.tntalert;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;

/**
 * Mi API para el sistema de alertas inmersivas.
 * Con ella, otros módulos o mods podrán enviar notificaciones en pantalla
 * de una manera centralizada y con diseños predefinidos.
 */
public interface TNTAlertApi {

    /**
     * Muestra una alerta a una colección de jugadores específicos.
     *
     * @param players   Los jugadores que verán la alerta.
     * @param type      El tipo de alerta (NOTIFICATION, WARNING, URGENT, TIP).
     * @param title     El texto principal de la alerta.
     * @param subtitle  El texto secundario o descripción.
     */
    void showAlert(Collection<ServerPlayerEntity> players, AlertType type, String title, String subtitle);

    /**
     * Muestra una alerta a todos los jugadores conectados en el servidor.
     *
     * @param type      El tipo de alerta.
     * @param title     El título de la alerta.
     * @param subtitle  El subtítulo de la alerta.
     */
    void showToAll(AlertType type, String title, String subtitle);
}