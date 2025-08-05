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
 * Implementación de la API de alertas de TNTStudios.
 * Cada tipo de alerta tiene su propio estilo, colores y animaciones,
 * diseñadas para ser profesionales y claras, sin efectos "snake".
 */
public class TNTAlertManager implements TNTAlertApi {

    private final MinecraftServer server;

    public TNTAlertManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void showAlert(Collection<ServerPlayerEntity> players, AlertType type, String title, String subtitle) {
        ImmersiveMessage msg = createMessageForType(type, title, subtitle);
        if (msg != null) {
            msg.sendServer((Collection) players);
        }
    }

    @Override
    public void showToAll(AlertType type, String title, String subtitle) {
        ImmersiveMessage msg = createMessageForType(type, title, subtitle);
        if (msg != null) {
            msg.sendServerToAll(server);
        }
    }

    // Fabrica de mensajes según tipo de alerta
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

    // Alerta estándar (informativa) en la parte superior
    private ImmersiveMessage createNotificationMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(5f, title)
                .anchor(TextAnchor.TOP_CENTER)
                .wrap(320)
                .background()
                .borderTopColor(0xFFA0A0A0)
                .borderBottomColor(0xFF707070)
                .backgroundColor(0x50000000)
                .slideDown(0.4f)
                .fadeIn(0.4f)
                .animation(anim -> anim.transition(Binding.Size, 0f, 0.3f, 0f, 1f, EasingType.EaseOutBack))
                .fadeOut(0.4f)
                .typewriter(20f, true)
                .sound(SoundEffect.LOWSHORT)
                .color(TextColor.fromFormatting(Formatting.WHITE))
                .bold()
                .subtext(0.2f, subtitle, sub -> sub
                        .anchor(TextAnchor.TOP_CENTER)
                        .wrap(320)
                        .fadeIn(0.4f)
                        .fadeOut(0.4f)
                        .typewriter(15f, true)
                        .color(TextColor.fromFormatting(Formatting.GRAY)));
    }

    // Alerta de advertencia con pulso sutil
    private ImmersiveMessage createWarningMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(6f, title)
                .anchor(TextAnchor.CENTER_CENTER)
                .wrap(280)
                .background()
                .borderTopColor(0xFFFFCC00)
                .borderBottomColor(0xFFD18B00)
                .backgroundColor(0x60FFEB3B)
                .fadeIn(0.3f)
                .animation(anim -> anim.transition(Binding.Size, 0f, 0.4f, 0.8f, 1f, EasingType.EaseOutBack))
                .fadeOut(0.3f)
                .typewriter(25f, true)
                .sound(SoundEffect.LOW)
                .color(TextColor.fromFormatting(Formatting.GOLD))
                .size(1.1f)
                .bold()
                .subtext(0.2f, subtitle, sub -> sub
                        .anchor(TextAnchor.CENTER_CENTER)
                        .wrap(280)
                        .fadeIn(0.3f)
                        .fadeOut(0.3f)
                        .typewriter(18f, true)
                        .color(TextColor.fromFormatting(Formatting.WHITE)));
    }

    // Alerta urgente con vibración y colores intensos
    private ImmersiveMessage createUrgentMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(7f, title)
                .anchor(TextAnchor.CENTER_CENTER)
                .wrap(260)
                .background()
                .borderTopColor(0xFFFF4444)
                .borderBottomColor(0xFFD50000)
                .backgroundColor(0x70FFCDD2)
                .fadeIn(0.3f)
                .animation(anim -> anim.transition(Binding.Size, 0f, 0.3f, 0.9f, 1f, EasingType.EaseOutBack))
                .fadeOut(0.3f)
                .typewriter(35f, true)
                .sound(SoundEffect.LOWSHORT)
                .color(TextColor.fromFormatting(Formatting.RED))
                .size(1.2f)
                .bold()
                .subtext(0.1f, subtitle, sub -> sub
                        .anchor(TextAnchor.CENTER_CENTER)
                        .wrap(260)
                        .fadeIn(0.3f)
                        .fadeOut(0.3f)
                        .typewriter(20f, true)
                        .color(TextColor.fromFormatting(Formatting.WHITE)));
    }

    // Consejo (TIP) en lateral derecho, texto ajustado para no desbordar
    private ImmersiveMessage createTipMessage(String title, String subtitle) {
        return ImmersiveMessage.builder(5.5f, title)
                .anchor(TextAnchor.TOP_CENTER)
                .wrap(240)
                .background()
                .borderTopColor(0xFF55CDEF)
                .borderBottomColor(0xFF0099CC)
                .backgroundColor(0x6033B5E5)
                .slideLeft(0.5f)
                .fadeIn(0.5f)
                .animation(anim -> anim.transition(Binding.Size, 0f, 0.4f, 0f, 1f, EasingType.EaseOutBack))
                .fadeOut(0.5f)
                .typewriter(12f, true)
                .sound(SoundEffect.LOW)
                .color(TextColor.fromFormatting(Formatting.AQUA))
                .italic()
                .subtext(0.2f, subtitle, sub -> sub
                        .anchor(TextAnchor.TOP_CENTER)
                        .wrap(240)
                        .fadeIn(0.5f)
                        .fadeOut(0.5f)
                        .typewriter(10f, true)
                        .color(TextColor.fromFormatting(Formatting.WHITE)));
    }
}
