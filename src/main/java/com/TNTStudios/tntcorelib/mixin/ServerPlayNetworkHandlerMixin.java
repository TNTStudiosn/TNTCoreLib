package com.TNTStudios.tntcorelib.mixin;

import com.TNTStudios.tntcorelib.Tntcorelib;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mi mixin para el manejador de red del jugador.
 * AHORA: Su única responsabilidad es cancelar el movimiento a nivel de servidor,
 * delegando la corrección visual a otro proceso para máxima eficiencia.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    /**
     * Intercepto los paquetes de movimiento del jugador.
     * Si el jugador está congelado, simplemente cancelo el evento.
     * Esto es súper ligero y evita que el servidor procese un movimiento inválido.
     * La corrección visual del cliente la haré en otro lugar más óptimo.
     */
    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        // Valido si el jugador debe estar congelado.
        if (Tntcorelib.getFreezeApi().isPlayerFrozen(this.player)) {
            // ¡Perfecto! Mi única responsabilidad aquí es anular el movimiento en el servidor.
            // Esto es lo más eficiente que puedo hacer.
            ci.cancel();
        }
    }
}