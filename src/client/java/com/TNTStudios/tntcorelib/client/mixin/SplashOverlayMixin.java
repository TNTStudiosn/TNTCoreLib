package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.LoadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Unique
    private boolean videoLoaded = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(MinecraftClient client,
                        ResourceReload reload,
                        Consumer<Optional<Throwable>> exceptionHandler,
                        boolean reloading,
                        CallbackInfo ci) {
        // Se inicializa en el render para garantizar que todo esté listo
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void initVideo(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!videoLoaded) {
            videoLoaded = LoadScreen.tryInitVideo();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterRender(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!videoLoaded) return;

        LoadScreen.markSplashDone();
        LoadScreen.render(
                ctx,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );

        // No cerramos aquí: lo hacemos en el redirect cuando isFinished() devuelva true
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"
            )
    )
    private void redirectSetOverlay(MinecraftClient client, Overlay overlay) {
        // Solo permitimos el cambio cuando el video y fade han terminado
        if (LoadScreen.isFinished()) {
            client.setOverlay(overlay); // usamos el overlay original de Mojang
        }
        // si no, no hacemos nada (bloqueamos el cambio)
    }

}
