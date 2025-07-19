package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.CustomMenuHandler;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.MenuConfig;
import com.TNTStudios.tntcorelib.client.modulo.custommenu.MenuScreenVideo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    private long fadeInStart;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        MenuScreenVideo.tryInitVideo();
        this.clearChildren();

        MenuConfig config = CustomMenuHandler.menuConfig;

        // ✅ Se usa el texto del JSON para los botones.
        Text entrarText = Text.literal(config.entrarButtonText);
        Text holyHostingText = Text.literal("HolyHosting");
        Text ajustesText = Text.literal("Ajustes");
        Text salirText = Text.literal("Salir de el juego");

        ButtonWidget.Builder entrarButton = ButtonWidget.builder(entrarText, button -> {
            ServerInfo serverInfo = new ServerInfo("Server", config.serverIp, false);
            ConnectScreen.connect(this, this.client, ServerAddress.parse(config.serverIp), serverInfo, false);
        });

        ButtonWidget.Builder holyHostingButton = ButtonWidget.builder(holyHostingText, button -> {
            this.client.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getOperatingSystem().open(URI.create("https://holy.gg/"));
                }
                this.client.setScreen(this);
            }, "https://holy.gg/", true));
        });

        ButtonWidget.Builder ajustesButton = ButtonWidget.builder(ajustesText, button -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        });

        ButtonWidget.Builder salirButton = ButtonWidget.builder(salirText, button -> {
            this.client.scheduleStop();
        });

        // Se agrupan los textos y constructores para pasarlos al gestor de diseño.
        List<Text> buttonTexts = List.of(entrarText, holyHostingText, ajustesText, salirText);
        List<ButtonWidget.Builder> builders = List.of(entrarButton, holyHostingButton, ajustesButton, salirButton);

        // ✅ Se aplica el layout seleccionado desde la configuración.
        applyLayout(config.layoutVersion, builders, buttonTexts);

        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.fadeInStart == 0L) {
            this.fadeInStart = Util.getMeasuringTimeMs();
        }
        float fade = (float)(Util.getMeasuringTimeMs() - this.fadeInStart) / 1000.0F;

        MenuScreenVideo.render(context, this.width, this.height);

        // La transparencia se aplica automáticamente a los widgets en super.render
        super.render(context, mouseX, mouseY, delta);

        ci.cancel();
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        MenuScreenVideo.stop();
    }

    /**
     * ✅ AMPLIADO: Gestor de diseños que ahora soporta 10 layouts diferentes.
     * Coloca los botones según la versión elegida y calcula su tamaño dinámicamente.
     * @param version La versión del layout (1-10).
     * @param builders Los constructores de los botones a posicionar.
     * @param buttonTexts Los textos de los botones para calcular el ancho.
     */
    private void applyLayout(int version, List<ButtonWidget.Builder> builders, List<Text> buttonTexts) {
        // --- Cálculo de Tamaño Dinámico ---
        int maxWidth = 0;
        for (Text text : buttonTexts) {
            // Se obtiene el ancho en píxeles de cada texto y se guarda el más grande.
            maxWidth = Math.max(maxWidth, this.textRenderer.getWidth(text));
        }

        // ✅ Se añade un padding (espacio extra) para que el texto no quede pegado al borde.
        int buttonWidth = maxWidth + 20;
        int buttonHeight = 20;
        int spacing = 4;
        int padding = 10; // Espacio desde los bordes de la pantalla.

        // Aseguramos que los botones no sean más anchos que la pantalla.
        buttonWidth = Math.min(buttonWidth, this.width - (padding * 2));

        for (int i = 0; i < builders.size(); i++) {
            ButtonWidget.Builder builder = builders.get(i);
            int x = 0, y = 0;

            switch (version) {
                case 2: // Diseño 2: Fila horizontal inferior.
                    int totalWidth_v2 = (builders.size() * buttonWidth) + ((builders.size() - 1) * spacing);
                    int startX_v2 = (this.width - totalWidth_v2) / 2;
                    y = this.height - buttonHeight - 30;
                    x = startX_v2 + (i * (buttonWidth + spacing));
                    break;

                case 3: // Diseño 3: Fila vertical izquierda.
                    x = padding;
                    y = (this.height / 4 + 48) + (i * (buttonHeight + spacing));
                    break;

                case 4: // Diseño 4: Grid 2x2 centrado.
                    int gridWidth = (2 * buttonWidth) + spacing;
                    int gridHeight = (2 * buttonHeight) + spacing;
                    int gridStartX = (this.width - gridWidth) / 2;
                    int gridStartY = (this.height - gridHeight) / 2;
                    int col_v4 = i % 2;
                    int row_v4 = i / 2;
                    x = gridStartX + (col_v4 * (buttonWidth + spacing));
                    y = gridStartY + (row_v4 * (buttonHeight + spacing));
                    break;

                case 5: // Diseño 5: Fila horizontal superior.
                    int totalWidth_v5 = (builders.size() * buttonWidth) + ((builders.size() - 1) * spacing);
                    int startX_v5 = (this.width - totalWidth_v5) / 2;
                    y = padding + 10; // Un poco más abajo que el borde.
                    x = startX_v5 + (i * (buttonWidth + spacing));
                    break;

                // --- ✨ NUEVOS DISEÑOS ✨ ---

                case 6: // Diseño 6: Fila vertical derecha.
                    x = this.width - buttonWidth - padding;
                    y = (this.height / 4 + 48) + (i * (buttonHeight + spacing));
                    break;

                case 7: // Diseño 7: En las 4 esquinas.
                    switch (i) {
                        case 0: // Superior izquierda
                            x = padding;
                            y = padding;
                            break;
                        case 1: // Superior derecha
                            x = this.width - buttonWidth - padding;
                            y = padding;
                            break;
                        case 2: // Inferior izquierda
                            x = padding;
                            y = this.height - buttonHeight - padding;
                            break;
                        case 3: // Inferior derecha
                            x = this.width - buttonWidth - padding;
                            y = this.height - buttonHeight - padding;
                            break;
                    }
                    break;

                case 8: // Diseño 8: Columnas divididas (2 a la izq, 2 a la der).
                    int yOffset_v8 = this.height / 2 - buttonHeight - spacing;
                    if (i < 2) { // Columna izquierda
                        x = padding + 20;
                        y = yOffset_v8 + (i * (buttonHeight + spacing));
                    } else { // Columna derecha
                        x = this.width - buttonWidth - padding - 20;
                        y = yOffset_v8 + ((i - 2) * (buttonHeight + spacing));
                    }
                    break;

                case 9: // Diseño 9: Fila horizontal centrada.
                    int totalWidth_v9 = (builders.size() * buttonWidth) + ((builders.size() - 1) * spacing);
                    int startX_v9 = (this.width - totalWidth_v9) / 2;
                    y = (this.height - buttonHeight) / 2;
                    x = startX_v9 + (i * (buttonWidth + spacing));
                    break;

                case 10: // Diseño 10: Pila vertical centrada y espaciada.
                    int totalHeight_v10 = (builders.size() * (buttonHeight + spacing * 4)); // Más espacio vertical.
                    int startY_v10 = (this.height - totalHeight_v10) / 2;
                    x = (this.width - buttonWidth) / 2;
                    y = startY_v10 + (i * (buttonHeight + spacing * 4));
                    break;

                case 1: // Diseño 1 (default): Pila vertical centrada.
                default:
                    int totalHeight = (builders.size() * buttonHeight) + ((builders.size() - 1) * spacing);
                    int startY = (this.height - totalHeight) / 2 + 24; // Moverlo un poco hacia abajo
                    x = (this.width - buttonWidth) / 2;
                    y = startY + (i * (buttonHeight + spacing));
                    break;
            }

            // Se construye y añade el botón con las dimensiones calculadas para el layout actual.
            this.addDrawableChild(builder.dimensions(x, y, buttonWidth, buttonHeight).build());
        }
    }
}