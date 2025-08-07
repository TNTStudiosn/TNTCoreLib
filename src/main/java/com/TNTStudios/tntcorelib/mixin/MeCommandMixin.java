// Ubicación: src/main/java/com/TNTStudios/tntcorelib/mixin/MeCommandMixin.java
package com.TNTStudios.tntcorelib.mixin;

import com.TNTStudios.tntcorelib.modulo.mecommand.MeCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mi mixin para la clase MeCommand.
 * Intercepto el método 'register' para evitar que el comando /me se registre
 * si está desactivado en mi configuración. Esta es la forma más limpia
 * y compatible de deshabilitarlo por completo.
 */
@Mixin(MeCommand.class)
public class MeCommandMixin {

    /**
     * Inyecto mi lógica al inicio del método de registro.
     * Si el comando no está habilitado en mi config, cancelo la ejecución del método
     * y el comando /me nunca llegará a existir en el dispatcher del servidor.
     */
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        // Primero, verifico que mi módulo de configuración se haya cargado.
        if (MeCommandHandler.getConfig() == null) {
            return; // Si no, dejo que se registre por seguridad.
        }

        // Si el comando está desactivado en mi config...
        if (!MeCommandHandler.getConfig().commandEnabled) {
            // ...cancelo el registro. ¡Adiós, /me!
            ci.cancel();
        }
    }
}