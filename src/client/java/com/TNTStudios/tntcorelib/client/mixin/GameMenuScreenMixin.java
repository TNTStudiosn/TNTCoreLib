// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/mixin/GameMenuScreenMixin.java
package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.PauseLogoManager;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.PauseMenuConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen; // Importación necesaria.
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Shadow @Final private boolean showMenu;
    @Shadow @Nullable private ButtonWidget exitButton;

    // Ya no necesito la referencia al método disconnect() original.
    // @Shadow protected abstract void disconnect();

    // Mis campos para almacenar las dimensiones y posición del logo ya escaladas.
    // Esto evita recalcular en cada frame en el método render().
    private int scaledLogoWidth;
    private int scaledLogoHeight;
    private int scaledLogoY;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    /**
     * Reemplazo el método init() para construir mi propio menú de pausa.
     * La nueva lógica utiliza posiciones fijas definidas en el config, que se escalan
     * de manera uniforme según el tamaño de la ventana para un diseño consistente.
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        // Me aseguro de que el logo esté cargado.
        PauseLogoManager.initialize();

        if (!this.showMenu) {
            return;
        }
        // Cancelo el método original para construir mi propio menú.
        ci.cancel();

        PauseMenuConfig config = CustomMenuHandler.pauseMenuConfig;

        // AJUSTE CLAVE: Lógica de escalado fijo.
        // Diseño todo para una altura de referencia (ej: 480p) y luego lo escalo
        // al tamaño actual de la ventana. Esto mantiene las proporciones y posiciones
        // sin importar la resolución, como si fuera una GUI a escala 3 fija.
        final float REFERENCE_HEIGHT = 480.0f;
        float scale = this.height / REFERENCE_HEIGHT;

        // --- Cálculo del logo escalado ---
        this.scaledLogoWidth = (int) (config.logoRenderWidth * scale);
        this.scaledLogoHeight = (int) (config.logoRenderHeight * scale);
        this.scaledLogoY = (int) (config.logoYPosition * scale);

        // --- Configuración de los botones ---
        final int BASE_BUTTON_WIDTH = 204;

        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(1);

        Text returnToGameText = Text.literal(config.returnToGameButtonText);
        Text optionsText = Text.literal(config.optionsButtonText);
        Text disconnectText = this.client.isInSingleplayer()
                ? Text.translatable("menu.returnToMenu")
                : Text.literal(config.disconnectButtonText);

        adder.add(ButtonWidget.builder(returnToGameText, button -> {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
        }).width(BASE_BUTTON_WIDTH).build());

        adder.add(ButtonWidget.builder(optionsText, button -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).width(BASE_BUTTON_WIDTH).build());

        this.exitButton = adder.add(ButtonWidget.builder(disconnectText, button -> {
            button.active = false;
            // Mi lógica de desconexión personalizada que siempre lleva al menú principal.
            this.client.getAbuseReportContext().tryShowDraftScreen(this.client, this, () -> {
                // Primero, desconecto la sesión del mundo actual.
                if (this.client.world != null) {
                    this.client.world.disconnect();
                }
                // Luego, le digo al cliente que limpie su estado y el servidor integrado (si existe).
                this.client.disconnect();
                // Finalmente, fuerzo la navegación a la pantalla de título principal.
                this.client.setScreen(new TitleScreen());
            }, true);
        }).width(BASE_BUTTON_WIDTH).build());


        // Es crucial llamar a refreshPositions() para que el widget calcule su altura final escalada.
        gridWidget.refreshPositions();

        // --- Posicionamiento fijo y escalado de los botones ---
        // Uso la posición Y de los botones del config y la escalo.
        int gridY = (int) (config.buttonsYPosition * scale);
        SimplePositioningWidget.setPos(gridWidget, 0, gridY, this.width, gridWidget.getHeight(), 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    /**
     * Sobrescribo el método render para dibujar mi logo personalizado con las dimensiones escaladas
     * y luego dejo que la clase Screen dibuje los botones ya posicionados.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.showMenu) {
            super.render(context, mouseX, mouseY, delta);
            return;
        }
        ci.cancel();

        // Dibujo el fondo oscuro por defecto.
        this.renderBackground(context); // Corregido para usar el método correcto

        Identifier logoTextureId = PauseLogoManager.getLogoTextureId();

        if (logoTextureId != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();

            int logoX = (this.width - this.scaledLogoWidth) / 2;

            // Dibujo el logo usando las dimensiones y posición Y calculadas y escaladas en init().
            context.drawTexture(logoTextureId, logoX, this.scaledLogoY, 0, 0, this.scaledLogoWidth, this.scaledLogoHeight, this.scaledLogoWidth, this.scaledLogoHeight);

            RenderSystem.disableBlend();
        } else {
            // Fallback si no hay logo, centrado con una posición Y fija.
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 70, 0xFFFFFF);
        }

        // Llamo al render de la superclase para que dibuje los widgets (botones).
        super.render(context, mouseX, mouseY, delta);

        // Dibujo el indicador de reportes pendientes si es necesario.
        if (this.client != null && this.client.getAbuseReportContext().hasDraft() && this.exitButton != null) {
            context.drawTexture(ButtonWidget.WIDGETS_TEXTURE, this.exitButton.getX() + this.exitButton.getWidth() - 17, this.exitButton.getY() + 3, 182, 24, 15, 15);
        }
    }
}