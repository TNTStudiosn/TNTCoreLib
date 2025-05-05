package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.LoadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    private boolean videoWasActive = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(MinecraftClient client,
                        ResourceReload reload,
                        Consumer<Optional<Throwable>> exceptionHandler,
                        boolean reloading,
                        CallbackInfo ci) {
        LoadScreen.tryInitVideo();
        videoWasActive = true;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderVideo(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LoadScreen.render(
                ctx,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void checkOverlayClosed(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (videoWasActive && MinecraftClient.getInstance().getOverlay() == null) {
            LoadScreen.stopVideo();
            videoWasActive = false;
        }
    }
}

