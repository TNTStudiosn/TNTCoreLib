package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.window.WindowConfig;
import com.TNTStudios.tntcorelib.client.modulo.window.WindowHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Mi mixin para la clase Window.
 * Aquí es donde hago la magia para cambiar el título y el icono de la ventana.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private long handle;
    // He eliminado el @Shadow al campo 'title' porque no existe en la clase original.

    /**
     * Intercepto CUALQUIER intento de cambiar el título de la ventana.
     * Minecraft no solo establece el título en el constructor, sino que lo actualiza
     * constantemente (por ejemplo, al entrar en un mundo).
     * Esta inyección se asegura de que nuestro título personalizado siempre tenga prioridad.
     *
     * @param newTitle El título que Minecraft intenta establecer. Lo ignoramos si nuestra config está activa.
     * @param ci CallbackInfo para cancelar el método original.
     */
    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void onSetTitle(String newTitle, CallbackInfo ci) {
        // Cargo mi configuración.
        WindowConfig config = WindowHandler.windowConfig;

        // Si la función de título personalizado está activa en mi config...
        if (config != null && config.useCustomTitle) {
            String customTitle = config.windowTitle;

            // Me aseguro de que el título no esté vacío para evitar problemas.
            if (customTitle != null && !customTitle.isEmpty()) {
                // Establezco mi título personalizado directamente con GLFW.
                // Esta es la única llamada necesaria para cambiar el título.
                GLFW.glfwSetWindowTitle(this.handle, customTitle);
            }

            // Cancelo el método original. Esto es CRUCIAL.
            // Impide que Minecraft sobrescriba mi título personalizado con el suyo.
            ci.cancel();
        }
        // Si mi función está desactivada, esta inyección no hace nada y se ejecuta el código original de Minecraft.
    }


    /**
     * Reemplazo el método setIcon para cargar mis propios iconos desde la carpeta de configuración.
     * Si la función está desactivada en mi config, simplemente no hago nada y dejo que el método original se ejecute.
     */
    @Inject(method = "setIcon", at = @At("HEAD"), cancellable = true)
    private void onSetIcon(ResourcePack resourcePack, Icons icons, CallbackInfo ci) {
        WindowConfig config = WindowHandler.windowConfig;

        // Solo actúo si tengo una configuración cargada y la función está activada.
        if (config == null || !config.useCustomIcon) {
            return;
        }

        // La lógica de Mac es diferente, por ahora la omito para mantenerlo simple y estable.
        if (MinecraftClient.IS_SYSTEM_MAC) {
            LOGGER.warn("[TNTCoreLib] El icono personalizado de ventana no es compatible con macOS por ahora.");
            ci.cancel(); // Cancelo para evitar que se ponga el icono por defecto.
            return;
        }

        RenderSystem.assertInInitPhase();

        // Preparo la lista de mis archivos de icono.
        List<File> iconFiles = new ArrayList<>();
        File iconDir = WindowHandler.CONFIG_DIR.toFile();
        if (config.icon16 != null && !config.icon16.isEmpty()) iconFiles.add(new File(iconDir, config.icon16));
        if (config.icon32 != null && !config.icon32.isEmpty()) iconFiles.add(new File(iconDir, config.icon32));
        if (config.icon256 != null && !config.icon256.isEmpty()) iconFiles.add(new File(iconDir, config.icon256));

        if (iconFiles.isEmpty()) {
            // Si no hay iconos en el config, no cancelo y dejo que Minecraft ponga los suyos.
            return;
        }

        List<ByteBuffer> allocatedBuffers = new ArrayList<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GLFWImage.Buffer imageBuffer = GLFWImage.malloc(iconFiles.size(), stack);
            int validIcons = 0;

            for (File iconFile : iconFiles) {
                if (!iconFile.exists()) {
                    LOGGER.warn("[TNTCoreLib] Archivo de icono no encontrado, saltando: {}", iconFile.getAbsolutePath());
                    continue;
                }

                // Cargo mi imagen PNG y la convierto a un formato que GLFW entienda.
                try (InputStream inputStream = new FileInputStream(iconFile);
                     NativeImage nativeImage = NativeImage.read(inputStream)) {

                    ByteBuffer byteBuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);
                    allocatedBuffers.add(byteBuffer);
                    byteBuffer.asIntBuffer().put(nativeImage.copyPixelsRgba());

                    imageBuffer.position(validIcons);
                    imageBuffer.width(nativeImage.getWidth());
                    imageBuffer.height(nativeImage.getHeight());
                    imageBuffer.pixels(byteBuffer.rewind()); // ¡Importante! Rebobino el buffer.
                    validIcons++;
                }
            }

            if (validIcons > 0) {
                // Le paso los iconos a GLFW.
                GLFW.glfwSetWindowIcon(this.handle, imageBuffer.position(0));
            }

        } catch (IOException e) {
            LOGGER.error("[TNTCoreLib] Fallo al cargar los iconos de ventana personalizados", e);
        } finally {
            // Libero la memoria que he reservado. ¡Muy importante para no causar fugas de memoria!
            allocatedBuffers.forEach(MemoryUtil::memFree);
        }

        // He terminado, así que cancelo el método original para que no sobrescriba mi trabajo.
        ci.cancel();
    }
}