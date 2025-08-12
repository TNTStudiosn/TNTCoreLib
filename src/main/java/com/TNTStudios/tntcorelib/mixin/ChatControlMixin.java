// Ubicación: src/main/java/com/TNTStudios/tntcorelib/mixin/ChatControlMixin.java
package com.TNTStudios.tntcorelib.mixin;

import com.TNTStudios.tntcorelib.Tntcorelib;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mi mixin para ServerPlayNetworkHandler.
 * Intercepto los paquetes de chat entrantes para decidir si deben procesarse o no,
 * basándome en la configuración de mi módulo ChatControl.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ChatControlMixin {

    @Shadow public ServerPlayerEntity player;

    /**
     * Inyecto mi lógica al inicio del método que maneja los mensajes de chat.
     * Si el chat está muteado y el jugador no tiene permisos, cancelo el evento.
     * Esta es la forma más eficiente de bloquear mensajes, ya que ocurre antes de
     * cualquier procesamiento en el servidor.
     */
    @Inject(method = "onChatMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V", at = @At("HEAD"), cancellable = true)
    private void tntcorelib$onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        // Valido si la API está lista para evitar errores durante el arranque.
        if (Tntcorelib.getChatControlApi() == null) {
            return;
        }

        // Si la API me dice que el chat está muteado...
        if (Tntcorelib.getChatControlApi().isChatMuted()) {
            // ...y el jugador no tiene el nivel de permiso requerido...
            if (!this.player.hasPermissionLevel(4)) {
                // ...le envío un mensaje de feedback y cancelo el envío de su mensaje.
                player.sendMessage(Text.literal("§cEl chat global está actualmente desactivado."), false);
                ci.cancel();
            }
        }
    }
}