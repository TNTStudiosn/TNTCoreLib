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
     * Mi inyección para controlar el renderizado de la barra de vida.
     * Cancelo el dibujado únicamente si la vida está pausada.
     * Esta es la forma más limpia, ya que solo afecta a los corazones.
     */
    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void tntcorelib$cancelHealthRendering(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        // Si la vida está pausada, no muestro la barra de vida.
        if (ClientStatsManager.isHealthPaused()) {
            ci.cancel();
        }
    }

    /**
     * Mi inyección para controlar el renderizado de la barra de comida (y aire).
     * Me engancho justo antes de la sección que dibuja la vida de la montura o la comida del jugador.
     * Si la comida está pausada, cancelo el resto del método, evitando que se dibuje tanto la comida como el aire.
     */
    @Inject(method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getRiddenEntity()Lnet/minecraft/entity/LivingEntity;"),
            cancellable = true
    )
    private void tntcorelib$cancelFoodAndAirRendering(DrawContext context, CallbackInfo ci) {
        // Si la comida está pausada, no muestro la barra de comida ni la de aire.
        if (ClientStatsManager.isFoodPaused()) {
            ci.cancel();
        }
    }
}