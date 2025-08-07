package com.TNTStudios.tntcorelib.mixin;

import com.TNTStudios.tntcorelib.Tntcorelib;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mi mixin para ServerPlayerEntity.
 * AHORA: No solo anula la velocidad, sino que también gestiona el teletransporte
 * correctivo de forma súper optimizada para evitar el rubber-banding.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerUpdateMixin {

    /**
     * Inyecto mi lógica al inicio del método 'tick'.
     * Aquí controlo todos los aspectos del estado "congelado".
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo info) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Si el jugador no está congelado, no hago nada.
        if (!Tntcorelib.getFreezeApi().isPlayerFrozen(player)) {
            return;
        }

        // Anulo cualquier velocidad para contrarrestar fuerzas externas.
        if (player.getVelocity().lengthSquared() > 0.0) {
            player.setVelocity(0, 0, 0);
        }

        // Reseteo la distancia de caída como medida de seguridad.
        player.fallDistance = 0.0f;

        // ✅ MI OPTIMIZACIÓN CLAVE:
        // Muevo la lógica de teletransporte aquí y la ejecuto de forma controlada.
        // Usando el contador de ticks del servidor (que es global), envío el paquete de corrección
        // solo 5 veces por segundo (20 ticks / 4 = 5). Esto es más que suficiente para
        // evitar el rubber-banding en el cliente sin inundar la red con paquetes.
        // Es la solución más eficiente para un gran número de jugadores.
        if (player.getServer().getTicks() % 4 == 0) {
            player.networkHandler.requestTeleport(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYaw(),
                    player.getPitch()
            );
        }
    }
}