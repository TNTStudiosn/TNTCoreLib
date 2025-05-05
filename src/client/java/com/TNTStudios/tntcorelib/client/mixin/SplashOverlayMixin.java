package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.LoadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    @Shadow private float progress;          // shadow del progreso interno

    private boolean loadedCustomVideo = false;
    private boolean splashEndedRequested = false;

    // 1) Inicia el vídeo la primera vez que renderiza el SplashOverlay
    @Inject(method = "render", at = @At("HEAD"))
    private void initVideo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!loadedCustomVideo) {
            loadedCustomVideo = LoadScreen.tryInitVideo();
        }
    }

    // 2) Tras que Mojang pinte TODO el splash, detectamos si progress>=1 y renderizamos nuestro vídeo
    @Inject(method = "render", at = @At("RETURN"))
    private void renderAndDetectEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!loadedCustomVideo) return;

        // cuando Mojang acaba (progress llega a 1), pedimos el fade solo una vez
        if (!splashEndedRequested && progress >= 1.0f) {
            splashEndedRequested = true;
            LoadScreen.requestFade();
        }

        // encima de todo pintamos el vídeo (o el último fotograma si ya terminó)
        LoadScreen.render(
                context,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );
    }
}