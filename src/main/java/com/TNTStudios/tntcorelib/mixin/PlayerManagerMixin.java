// Ubicación: src/main/java/com/TNTStudios/tntcorelib/mixin/PlayerManagerMixin.java
package com.TNTStudios.tntcorelib.mixin;

import com.TNTStudios.tntcorelib.modulo.connectionmessages.ConnectionMessagesHandler;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Set;

/**
 * Mi mixin para PlayerManager.
 * Intercepto el método 'broadcast' para poder cancelar los mensajes de conexión
 * si están desactivados en mi configuración.
 */
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    // Defino los translation keys de los mensajes que quiero interceptar.
    private static final Set<String> CONNECTION_MESSAGE_KEYS = Set.of(
            "multiplayer.player.joined",
            "multiplayer.player.joined.renamed",
            "multiplayer.player.left"
    );

    /**
     * Inyecto mi lógica al inicio del método 'broadcast'.
     * Si los mensajes están desactivados y el texto que se va a enviar es un mensaje
     * de conexión, cancelo la operación.
     */
    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    private void tntcorelib$onBroadcast(Text message, boolean overlay, CallbackInfo ci) {
        // Primero, verifico que mi módulo de configuración se haya cargado.
        if (ConnectionMessagesHandler.getConfig() == null) {
            return;
        }

        // Si los mensajes están desactivados...
        if (!ConnectionMessagesHandler.getConfig().messagesEnabled) {
            // ...y el mensaje es de tipo traducible (como los de conexión)...
            if (message.getContent() instanceof TranslatableTextContent translatable) {
                // ...y su clave está en mi lista de claves de conexión...
                if (CONNECTION_MESSAGE_KEYS.contains(translatable.getKey())) {
                    // ...entonces cancelo el envío del mensaje.
                    ci.cancel();
                }
            }
        }
    }
}