package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.tablist.CustomPlayerListHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mi mixin para la clase Mouse.
 * Lo uso para detectar el scroll del ratón y poder cambiar de página
 * en mi tablist personalizado solo cuando este está visible.
 */
@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        // Verifico si el tablist está visible. Como el campo 'visible' es privado en PlayerListHud,
        // confío en que si la opción 'playerListKey' está presionada, es porque el tablist está activo.
        if (this.client.options.playerListKey.isPressed() && this.client.inGameHud != null) {
            // Le paso el valor del scroll a mi tablist para que cambie de página.
            CustomPlayerListHud.onScroll(vertical);

            // Cancelo el evento para que no se mueva el hotbar mientras veo la lista de jugadores.
            ci.cancel();
        }
    }
}