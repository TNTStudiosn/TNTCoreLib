// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/modulo/custommenu/PauseLogoManager.java
package com.TNTStudios.tntcorelib.client.modulo.custommenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture; // ¡Import corregido!
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Mi gestor para el logo del menú de pausa.
 * Se encarga de cargar la imagen PNG desde la carpeta de configuración
 * y la registra como una textura dinámica en Minecraft.
 */
public class PauseLogoManager {

    @Nullable
    private static Identifier registeredLogoId = null;
    private static boolean initialized = false;

    /**
     * Intenta cargar el logo desde el archivo especificado en la configuración.
     * Si tiene éxito, registra la imagen como una textura de Minecraft.
     * Es seguro llamar a este método varias veces.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            if (!CustomMenuHandler.PAUSE_LOGO_FILE.exists()) {
                System.err.println("[TNTCoreLib] No se encontró el archivo del logo en: " + CustomMenuHandler.PAUSE_LOGO_FILE.getAbsolutePath());
                return;
            }

            try (InputStream inputStream = new FileInputStream(CustomMenuHandler.PAUSE_LOGO_FILE)) {
                NativeImage image = NativeImage.read(inputStream);
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                // Ahora uso NativeImageBackedTexture, que es la clase correcta para esto.
                NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(image);

                // Registro la textura y guardo su identificador para poder usarla después.
                // Este método funciona perfectamente con NativeImageBackedTexture.
                registeredLogoId = textureManager.registerDynamicTexture("pause_logo_dynamic", dynamicTexture);
                System.out.println("[TNTCoreLib] Logo del menú de pausa cargado y registrado exitosamente.");
            }

        } catch (IOException e) {
            System.err.println("[TNTCoreLib] Error al cargar el logo del menú de pausa.");
            e.printStackTrace();
            registeredLogoId = null;
        }
    }

    /**
     * Devuelve el identificador de la textura del logo si se cargó correctamente.
     * @return El Identifier del logo, o null si no se pudo cargar.
     */
    @Nullable
    public static Identifier getLogoTextureId() {
        return registeredLogoId;
    }
}