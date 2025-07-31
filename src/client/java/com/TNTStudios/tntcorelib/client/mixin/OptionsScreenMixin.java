// Ubicación: src/client/java/com/TNTStudios/tntcorelib/mixin/client/OptionsScreenMixin.java
package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.OptionsConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    // Hago Shadow de los campos que necesito de la clase original.
    @Shadow @Final private Screen parent;
    @Shadow @Final private GameOptions settings;

    // Defino los textos estáticos para no tener que recrearlos y evitar errores.
    @Shadow @Final private static Text SKIN_CUSTOMIZATION_TEXT;
    @Shadow @Final private static Text SOUNDS_TEXT;
    @Shadow @Final private static Text VIDEO_TEXT;
    @Shadow @Final private static Text CONTROL_TEXT;
    @Shadow @Final private static Text LANGUAGE_TEXT;
    @Shadow @Final private static Text CHAT_TEXT;
    @Shadow @Final private static Text RESOURCE_PACK_TEXT;
    @Shadow @Final private static Text ACCESSIBILITY_TEXT;
    @Shadow @Final private static Text TELEMETRY_TEXT;
    @Shadow @Final private static Text CREDITS_AND_ATTRIBUTION_TEXT;

    // Métodos privados de la clase original que necesito llamar.
    @Shadow protected abstract ButtonWidget createButton(Text message, Supplier<Screen> screenSupplier);
    @Shadow protected abstract Widget createTopRightButton();
    @Shadow protected abstract void refreshResourcePacks(ResourcePackManager resourcePackManager);

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    /**
     * Inyecto mi propia lógica al principio del método init() y lo cancelo
     * para reemplazarlo por completo. De esta forma, construyo el menú
     * dinámicamente según mi archivo de configuración.
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        // Cargo mi configuración personalizada.
        OptionsConfig config = CustomMenuHandler.optionsConfig;

        // Creo el GridWidget igual que en la clase original.
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        // Añado el slider de FOV, que siempre está presente.
        adder.add(this.settings.getFov().createWidget(this.client.options, 0, 0, 150));

        // Ahora manejo el botón superior derecho (Dificultad o Partidas en Línea)
        // La lógica original para esto está en createTopRightButton().
        if (this.client.world != null && this.client.isIntegratedServerRunning()) {
            // Si estoy en un mundo de un jugador, muestro siempre el botón de dificultad.
            adder.add(this.createTopRightButton());
        } else {
            // Si estoy en el menú principal, el botón es "Online".
            // Lo muestro solo si está activado en mi config.
            if (config.onlineButton) {
                adder.add(this.createTopRightButton());
            } else {
                // Si está desactivado, añado un widget vacío para no romper el layout del grid.
                // El ancho 150 es el que usa el botón original.
                adder.add(net.minecraft.client.gui.widget.EmptyWidget.ofWidth(150));
            }
        }

        // Añado un espacio para separar los botones principales.
        adder.add(net.minecraft.client.gui.widget.EmptyWidget.ofHeight(26), 2);

        // Ahora, añado los botones solo si están activados en la configuración.
        // El GridWidget se encarga de reordenarlos automáticamente.
        if (config.skinCustomizationButton) {
            adder.add(createButton(SKIN_CUSTOMIZATION_TEXT, () -> new SkinOptionsScreen(this, this.settings)));
        }
        if (config.soundsButton) {
            adder.add(createButton(SOUNDS_TEXT, () -> new SoundOptionsScreen(this, this.settings)));
        }
        if (config.videoButton) {
            adder.add(createButton(VIDEO_TEXT, () -> new VideoOptionsScreen(this, this.settings)));
        }
        if (config.controlsButton) {
            adder.add(createButton(CONTROL_TEXT, () -> new ControlsOptionsScreen(this, this.settings)));
        }
        if (config.languageButton) {
            adder.add(createButton(LANGUAGE_TEXT, () -> new LanguageOptionsScreen(this, this.settings, this.client.getLanguageManager())));
        }
        if (config.chatButton) {
            adder.add(createButton(CHAT_TEXT, () -> new ChatOptionsScreen(this, this.settings)));
        }
        if (config.resourcePackButton) {
            adder.add(createButton(RESOURCE_PACK_TEXT, () -> new PackScreen(this.client.getResourcePackManager(), this::refreshResourcePacks, this.client.getResourcePackDir(), Text.translatable("resourcePack.title"))));
        }
        if (config.accessibilityButton) {
            adder.add(createButton(ACCESSIBILITY_TEXT, () -> new AccessibilityOptionsScreen(this, this.settings)));
        }
        if (config.telemetryButton) {
            adder.add(createButton(TELEMETRY_TEXT, () -> new TelemetryInfoScreen(this, this.settings)));
        }
        if (config.creditsButton) {
            adder.add(createButton(CREDITS_AND_ATTRIBUTION_TEXT, () -> new CreditsAndAttributionScreen(this)));
        }

        // Añado el botón "Hecho" al final.
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.parent)).width(200).build(), 2, adder.copyPositioner().marginTop(6));

        // Refresco y posiciono el grid, igual que en el original.
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
        gridWidget.forEachChild(this::addDrawableChild);

        // Cancelo el método original para que mi implementación sea la única que se ejecute.
        ci.cancel();
    }
}