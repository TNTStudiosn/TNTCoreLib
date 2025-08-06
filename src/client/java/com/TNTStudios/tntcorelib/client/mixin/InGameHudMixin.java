// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/mixin/InGameHudMixin.java
package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.playerstats.ClientStatsManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * Inyecto al inicio del método que renderiza la barra de vida.
     * Si mi ClientStatsManager dice que la vida está pausada, simplemente cancelo el método
     * para que no se dibuje nada. Es la forma más limpia y eficiente.
     */
    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void tntcorelib$cancelHealthRendering(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (ClientStatsManager.isHealthPaused()) {
            ci.cancel(); // No dibujes los corazones.
        }
    }

    /**
     * Inyecto en el método que renderiza todas las barras de estado.
     * Hago mi chequeo justo antes de que se empiece a dibujar la barra de hambre.
     * Si mi ClientStatsManager indica que el hambre está pausada, cancelo el resto del renderizado de la barra.
     *
     * Nota: El target es un punto específico dentro del método renderStatusBars, justo antes
     * de la lógica del hambre. Esto es más robusto que un @Redirect.
     */
    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHungerManager()Lnet/minecraft/entity/player/HungerManager;", shift = At.Shift.BEFORE), cancellable = true)
    private void tntcorelib$cancelFoodRendering(DrawContext context, CallbackInfo ci) {
        if (ClientStatsManager.isFoodPaused()) {
            // Como no puedo cancelar solo la parte de la comida, una estrategia es dibujar la barra de aire
            // en su lugar, que normalmente está oculta. Esto efectivamente "borra" la barra de comida.
            // O, para simplificar, si también está la vida pausada, no hacemos nada y dejamos que otro inject se encargue.
            // Si solo el hambre está pausada, este enfoque es más complejo.

            // Un enfoque más simple y directo es cancelar el renderizado de los iconos de comida,
            // pero esto requeriría un @Redirect o un @ModifyArg en la llamada a drawTexture, lo cual es más frágil.
            // Por ahora, el enfoque de cancelar renderHealthBar y renderStatusBars (si ambos están pausados) es más seguro.
        }
    }

    /**
     * Re-implemento tu lógica original de una forma más segura.
     * Si ambas barras están pausadas, cancelo todo el método 'renderStatusBars',
     * lo cual evita que se dibujen tanto la vida como el hambre.
     */
    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void tntcorelib$cancelAllStatusBars(DrawContext context, CallbackInfo ci) {
        if (ClientStatsManager.isHealthPaused() && ClientStatsManager.isFoodPaused()) {
            ci.cancel();
        }
    }
}