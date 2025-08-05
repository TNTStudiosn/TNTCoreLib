package com.TNTStudios.tntcorelib.modulo.tntalert;

import com.TNTStudios.tntcorelib.api.tntalert.AlertType;
import com.TNTStudios.tntcorelib.api.tntalert.TNTAlertApi;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import toni.immersivemessages.api.ImmersiveMessage;
import toni.immersivemessages.api.TextAnchor;

import java.util.Collection;

/**
 * Mi implementación de la API de alertas.
 * Aquí es donde uso la librería ImmersiveMessages para diseñar y enviar
 * cada tipo de notificación con un estilo único.
 */
public class TNTAlertManager implements TNTAlertApi {

    private final MinecraftServer server;

    public TNTAlertManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void showAlert(Collection<ServerPlayerEntity> players, AlertType type, String title, String subtitle) {
        ImmersiveMessage message = createMessageForType(type, title, subtitle);
        if (message != null) {
            // La función sendServer espera una colección de ServerPlayer, no ServerPlayerEntity
            // Este es un supuesto basado en el código de ImmersiveMessage, puede que necesites un casting o conversión.
            message.sendServer((Collection) players);
        }
    }

    @Override
    public void showToAll(AlertType type, String title, String subtitle) {
        ImmersiveMessage message = createMessageForType(type, title, subtitle);
        if (message != null) {
            message.sendServerToAll(server);
        }
    }

    /**
     * Mi fábrica de mensajes. Dependiendo del tipo de alerta, creo un diseño diferente.
     * Esto mantiene el código organizado y facilita añadir nuevos estilos en el futuro.
     */
    private ImmersiveMessage createMessageForType(AlertType type, String title, String subtitle) {
        switch (type) {
            case NOTIFICATION:
                return createNotificationMessage(title, subtitle);
            case WARNING:
                return createWarningMessage(title, subtitle);
            case URGENT:
                return createUrgentMessage(title, subtitle);
            case TIP:
                return createTipMessage(title, subtitle);
            default:
                return null;
        }
    }

    // Diseño para una notificación estándar tipo "toast".
    private ImmersiveMessage createNotificationMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(5f, title)
                .anchor(TextAnchor.TOP_RIGHT).x(-10f).y(10f)
                .background().borderTopColor(0xFF888888).borderBottomColor(0xFF444444)
                .slideLeft(0.4f).slideOutRight(0.4f).fadeIn(0.5f).fadeOut(0.5f)
                .color(TextColor.fromFormatting(Formatting.WHITE)).bold()
                .subtext(0f, subtitle, 12f, sub -> sub
                        .anchor(TextAnchor.TOP_RIGHT).x(-10f)
                        .color(TextColor.fromFormatting(Formatting.GRAY))
                        .slideLeft(0.4f).slideOutRight(0.4f).fadeIn(0.5f).fadeOut(0.5f));
    }

    // Diseño para una advertencia. Más centrada y con colores llamativos.
    private ImmersiveMessage createWarningMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(6f, title)
                .anchor(TextAnchor.CENTER_CENTER).y(40f)
                .background().borderTopColor(0xFFFFF055).borderBottomColor(0xFFE5A500)
                .slideDown(0.5f).slideOutUp(0.5f).fadeIn().fadeOut()
                .shake(40f, 0.4f)
                .color(TextColor.fromFormatting(Formatting.YELLOW)).size(1.1f).bold()
                .subtext(0f, subtitle, 14f, sub -> sub
                        .anchor(TextAnchor.CENTER_CENTER)
                        .color(TextColor.fromFormatting(Formatting.WHITE))
                        .slideDown(0.5f).slideOutUp(0.5f).fadeIn().fadeOut());
    }

    // Diseño para una alerta urgente. Grande, roja y con efectos agresivos.
    private ImmersiveMessage createUrgentMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(8f, title)
                .anchor(TextAnchor.CENTER_CENTER)
                .background().rainbow(2.5f) // Borde arcoíris
                .slideUp(0.3f).slideOutDown(0.3f).fadeIn().fadeOut()
                .shake(120f, 0.8f)
                .color(TextColor.fromFormatting(Formatting.RED)).size(1.3f).bold()
                .subtext(0f, subtitle, 16f, sub -> sub
                        .anchor(TextAnchor.CENTER_CENTER)
                        .color(TextColor.fromFormatting(Formatting.WHITE))
                        .slideUp(0.3f).slideOutDown(0.3f).fadeIn().fadeOut());
    }

    // Diseño para un consejo. Discreto y en una esquina inferior.
    private ImmersiveMessage createTipMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(7f, "TIP: " + title)
                .anchor(TextAnchor.BOTTOM_LEFT).x(10f).y(-10f)
                .background().borderTopColor(0xFF55FFBB).borderBottomColor(0xFF00AA88)
                .slideUp(0.5f).slideOutDown(0.5f).fadeIn().fadeOut()
                .color(TextColor.fromFormatting(Formatting.AQUA)).italic()
                .subtext(0f, subtitle, 12f, sub -> sub
                        .anchor(TextAnchor.BOTTOM_LEFT).x(10f)
                        .color(TextColor.fromFormatting(Formatting.WHITE))
                        .slideUp(0.5f).slideOutDown(0.5f).fadeIn().fadeOut());
    }
}