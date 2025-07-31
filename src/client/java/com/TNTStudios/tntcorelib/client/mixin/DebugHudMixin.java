package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.debug.CustomDebugHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Inyecto mi código al inicio del método `render` original.
     * Si el F3 está activo, cancelo el renderizado de Minecraft y llamo a mi propia clase.
     * Esto me da control total sobre la pantalla de depuración y, como efecto secundario,
     * deshabilita el gráfico de perfilado (Shift+F3) porque el método original nunca se ejecuta.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, CallbackInfo ci) {
        // Solo actúo si el HUD de depuración está habilitado en las opciones.
        if (this.client.options.debugEnabled) {
            // Cancelo el renderizado original de Minecraft. Adiós, F3 feo.
            ci.cancel();

            // Renderizo mi propia interfaz, mucho más chula.
            CustomDebugHud.render(context);
        } else {
            // Si el F3 no está activo, me aseguro de que mi HUD sepa que debe cerrarse.
            CustomDebugHud.onClose();
        }
    }
}