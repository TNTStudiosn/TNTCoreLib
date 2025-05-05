// src/main/java/com/TNTStudios/tntcorelib/client/mixin/SplashOverlayMixin.java
package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.LoadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Unique private boolean videoLoaded = false;

    // Arrancar la carga del video justo antes de renderizar
    @Inject(method = "render", at = @At("HEAD"))
    private void initVideo(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!videoLoaded) {
            videoLoaded = LoadScreen.tryInitVideo();
        }
    }

    // Después de la renderización original, superponer el video si corresponde
    @Inject(method = "render", at = @At("RETURN"))
    private void afterRender(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!videoLoaded) return;

        // Avisar que la parte splash de Mojang terminó
        LoadScreen.markSplashDone();

        // Renderizar nuestro video
        LoadScreen.render(
                ctx,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );

        // Si ya terminó, quitar el overlay para pasar al menú
        if (LoadScreen.isFinished()) {
            MinecraftClient.getInstance().setOverlay(null);
        }
    }

    // Evitar que Mojang cierre el overlay hasta que nuestro video termine
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"
            )
    )
    private void redirectSetOverlay(MinecraftClient client, Overlay overlay) {
        if (LoadScreen.isFinished()) {
            client.setOverlay(overlay);
        }
    }
}
