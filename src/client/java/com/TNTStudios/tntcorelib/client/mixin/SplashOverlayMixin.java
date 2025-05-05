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

    @Shadow private float progress;

    private boolean loadedCustomVideo = false;
    private boolean splashEndedRequested = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void initVideo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!loadedCustomVideo) {
            loadedCustomVideo = LoadScreen.tryInitVideo();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderAndDetectEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!loadedCustomVideo) return;

        if (!splashEndedRequested && progress >= 1.0f) {
            splashEndedRequested = true;
            LoadScreen.requestFade();
        }

        LoadScreen.render(
                context,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );
    }
}
