// Ubicación: src/client/java/com/TNTStudios/tntcorelib/client/mixin/GameMenuScreenMixin.java
package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.PauseLogoManager;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.PauseMenuConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
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

    @Shadow protected abstract void disconnect();

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    /**
     * Reemplazo el método init() para construir mi propio menú de pausa.
     * Uso un GridWidget para alinear los 3 botones verticalmente.
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        // Me aseguro de que el logo esté cargado.
        PauseLogoManager.initialize();

        if (!this.showMenu) {
            return;
        }

        PauseMenuConfig config = CustomMenuHandler.pauseMenuConfig;
        final int BUTTON_WIDTH = 204;

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
        }).width(BUTTON_WIDTH).build());

        adder.add(ButtonWidget.builder(optionsText, button -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).width(BUTTON_WIDTH).build());

        this.exitButton = adder.add(ButtonWidget.builder(disconnectText, button -> {
            button.active = false;
            this.client.getAbuseReportContext().tryShowDraftScreen(this.client, this, this::disconnect, true);
        }).width(BUTTON_WIDTH).build());

        gridWidget.refreshPositions();
        // El ancla vertical 0.65F lo sitúa en la parte inferior de la pantalla, debajo del logo.
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5F, 0.65F);
        gridWidget.forEachChild(this::addDrawableChild);

        ci.cancel();
    }

    /**
     * Sobrescribo el método render para dibujar mi logo personalizado.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.showMenu) {
            this.renderBackground(context);

            Identifier logoTextureId = PauseLogoManager.getLogoTextureId();
            PauseMenuConfig config = CustomMenuHandler.pauseMenuConfig;

            if (logoTextureId != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                int logoWidth = config.logoRenderWidth;
                int logoHeight = config.logoRenderHeight;
                int logoX = (this.width - logoWidth) / 2;

                // Calculo la posición Y para centrar el logo en el cuarto superior de la pantalla.
                // Esto lo hace dinámico y responsivo al tamaño de la ventana.
                int logoY = this.height / 4 - logoHeight / 2;

                context.drawTexture(logoTextureId, logoX, logoY, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);

                RenderSystem.disableBlend();
            } else {
                // Si el logo no se carga, muestro el título original para que no quede vacío.
                context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
            }
        }

        // Llamo al render de la superclase (Screen) para que dibuje los widgets (botones).
        super.render(context, mouseX, mouseY, delta);

        if (this.showMenu && this.client != null && this.client.getAbuseReportContext().hasDraft() && this.exitButton != null) {
            context.drawTexture(ButtonWidget.WIDGETS_TEXTURE, this.exitButton.getX() + this.exitButton.getWidth() - 17, this.exitButton.getY() + 3, 182, 24, 15, 15);
        }

        ci.cancel();
    }
}