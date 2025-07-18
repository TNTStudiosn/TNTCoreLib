package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.LoadScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Mi mixin para SplashOverlay.
 * El objetivo es reemplazar la pantalla de carga de Mojang por un video personalizado.
 * He mejorado la versión anterior para asegurar que el video se renderice correctamente
 * y se detenga de forma sincronizada con el ciclo de vida del overlay.
 */
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    // Accedo a los campos de la clase original para replicar su lógica de tiempo.
    // Esto es mucho más fiable que usar flags y comprobar si el overlay es nulo.
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private ResourceReload reload;
    @Shadow private long reloadCompleteTime;

    /**
     * Inyecto código al final del constructor para inicializar mi video.
     * Esto solo se ejecuta una vez, cuando se crea el SplashOverlay.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(MinecraftClient client,
                        ResourceReload reload,
                        Consumer<Optional<Throwable>> exceptionHandler,
                        boolean reloading,
                        CallbackInfo ci) {
        // Intento inicializar el video. Mi clase LoadScreen se encarga de los detalles.
        LoadScreen.tryInitVideo();
    }

    /**
     * Inyecto código al inicio del método render() para tomar control total.
     * Uso cancellable = true para poder detener la ejecución del método original
     * y evitar que se dibuje el logo de Mojang.
     *
     * @param context El contexto de dibujado.
     * @param mouseX La posición X del ratón.
     * @param mouseY La posición Y del ratón.
     * @param delta El tiempo parcial entre frames.
     * @param ci El objeto CallbackInfo que me permite cancelar el método.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCustomVideo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long currentTime = Util.getMeasuringTimeMs();

        // Verifico si la recarga de recursos ha finalizado para marcar el tiempo de completado.
        // Esta es la misma lógica que usa el SplashOverlay original para saber cuándo empezar a desvanecerse.
        if (this.reloadCompleteTime == -1L && this.reload.isComplete()) {
            this.reloadCompleteTime = currentTime;
        }

        // Si la recarga ya terminó, compruebo si es hora de cerrar el overlay.
        if (this.reloadCompleteTime != -1L) {
            float timeSinceComplete = (float)(currentTime - this.reloadCompleteTime) / 1000.0F;

            // El overlay original se cierra después de 2 segundos.
            // Cuando eso ocurra, detengo mi video y le digo al cliente que quite el overlay.
            if (timeSinceComplete >= 2.0F) {
                LoadScreen.stopVideo(); // Detengo el video para liberar recursos.
                this.client.setOverlay(null); // Le digo a Minecraft que ya no hay overlay.
                ci.cancel(); // Cancelo el método para que no haga nada más.
                return; // Salgo del método inyectado.
            }
        }

        // Si el overlay todavía no debe cerrarse, renderizo un frame de mi video.
        LoadScreen.render(
                context,
                this.client.getWindow().getScaledWidth(),
                this.client.getWindow().getScaledHeight()
        );

        // Cancelo el resto del método original para prevenir que se renderice el logo de Mojang
        // y la barra de progreso sobre mi video.
        ci.cancel();
    }
}