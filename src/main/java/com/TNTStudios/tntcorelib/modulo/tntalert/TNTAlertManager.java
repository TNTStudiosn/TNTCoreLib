package com.TNTStudios.tntcorelib.modulo.tntalert;

import com.TNTStudios.tntcorelib.api.tntalert.AlertType;
import com.TNTStudios.tntcorelib.api.tntalert.TNTAlertApi;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import toni.immersivemessages.api.ImmersiveMessage;
import toni.immersivemessages.api.ObfuscateMode;
import toni.immersivemessages.api.SoundEffect;
import toni.immersivemessages.api.TextAnchor;
import toni.lib.animation.Binding;
import toni.lib.animation.easing.EasingType;


import java.util.Collection;

/**
 * Mi implementación de la API de alertas.
 * Aquí es donde uso la librería ImmersiveMessages para diseñar y enviar
 * cada tipo de notificación con un estilo único y profesional.
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
            // La función sendServer espera una colección de ServerPlayer.
            // El casteo a (Collection) es una forma de pasar la colección de ServerPlayerEntity.
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

    // Diseño para una notificación estándar en la parte superior central.
    private ImmersiveMessage createNotificationMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(6f, title)
                .anchor(TextAnchor.TOP_CENTER).y(20f) // Anclado arriba y al centro. [cite: 177, 183]
                .background() // Fondo sutil. [cite: 54]
                .borderTopColor(0x90666666).borderBottomColor(0x90333333)
                .backgroundColor(0x50000000)
                .slideDown(0.5f).slideOutUp(0.5f) // Animación de entrada y salida vertical. [cite: 98, 104]
                .fadeIn(0.5f).fadeOut(0.5f)
                .sound(SoundEffect.LOW).typewriter(15f, true) // Sonido de tipeo calmado. [cite: 69, 173, 53]
                .color(TextColor.fromFormatting(Formatting.WHITE)).bold()
                .subtext(0f, subtitle, 12f, sub -> sub
                        .anchor(TextAnchor.TOP_CENTER)
                        .color(TextColor.fromFormatting(Formatting.GRAY))
                        .slideDown(0.5f).slideOutUp(0.5f).fadeIn(0.5f).fadeOut(0.5f));
    }

    // Diseño para una advertencia. Centrada, con colores llamativos y una animación de "pop".
    private ImmersiveMessage createWarningMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(7f, title)
                .anchor(TextAnchor.CENTER_CENTER).y( -30f) // Anclado en el centro de la pantalla. [cite: 177]
                .background().borderTopColor(0xFFFFAA00).borderBottomColor(0xFFD48808)
                .backgroundColor(0x60331d00)
                .fadeIn(0.4f).fadeOut(0.4f)
                .sound(SoundEffect.LOW).typewriter(25f, true) // Sonido de tipeo más rápido.
                .animation(anim -> anim.transition(Binding.Size, 0, 0.6f, 0.5f, 1.1f, EasingType.EaseOutBack)) // Animación de "pop" al aparecer. [cite: 123]
                .color(TextColor.fromFormatting(Formatting.GOLD)).size(1.1f).bold()
                .subtext(0f, subtitle, 14f, sub -> sub
                        .anchor(TextAnchor.CENTER_CENTER)
                        .color(TextColor.fromFormatting(Formatting.WHITE))
                        .fadeIn(0.4f).fadeOut(0.4f)
                        .animation(anim -> anim.transition(Binding.Size, 0, 0.6f, 0.5f, 1.0f, EasingType.EaseOutBack)));
    }

    // Diseño para una alerta urgente. Grande, roja, con borde arcoíris y un revelado dramático.
    private ImmersiveMessage createUrgentMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(8f, title)
                .anchor(TextAnchor.CENTER_CENTER) // Máxima atención en el centro. [cite: 177]
                .background().rainbow(3f) // Borde arcoíris para destacar. [cite: 56, 57]
                .backgroundColor(0x70110000)
                .obfuscate(ObfuscateMode.RANDOM, 4f) // Texto se revela de forma aleatoria. [cite: 129, 131]
                .slideUp(0.4f).slideOutDown(0.4f).fadeIn(0.6f).fadeOut(0.4f)
                .sound(SoundEffect.LOWSHORT).typewriter(40f, true) // Sonido de tipeo rápido y agudo. [cite: 173]
                .color(TextColor.fromFormatting(Formatting.RED)).size(1.4f).bold()
                .subtext(0f, subtitle, 18f, sub -> sub
                        .anchor(TextAnchor.CENTER_CENTER)
                        .color(TextColor.fromFormatting(Formatting.WHITE))
                        .slideUp(0.4f).slideOutDown(0.4f).fadeIn(0.6f).fadeOut(0.4f));
    }

    // Diseño para un consejo. Discreto, en la parte inferior central y con colores agradables.
    private ImmersiveMessage createTipMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(7f, "TIP: " + title)
                .anchor(TextAnchor.BOTTOM_CENTER).y(-40f) // Anclado abajo y al centro. [cite: 177, 180]
                .background().borderTopColor(0xFF55FFBB).borderBottomColor(0xFF00AA88)
                .backgroundColor(0x6000221a)
                .slideUp(0.5f).slideOutDown(0.5f).fadeIn(0.5f).fadeOut(0.5f)
                .sound(SoundEffect.LOW).typewriter(10f, true) // Sonido de tipeo muy lento y suave.
                .color(TextColor.fromFormatting(Formatting.AQUA)).italic()
                .subtext(0f, subtitle, 12f, sub -> sub
                        .anchor(TextAnchor.BOTTOM_CENTER)
                        .color(TextColor.fromFormatting(Formatting.WHITE))
                        .slideUp(0.5f).slideOutDown(0.5f).fadeIn(0.5f).fadeOut(0.5f));
    }
}