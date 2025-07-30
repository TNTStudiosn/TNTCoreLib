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

        List<Text> buttonTexts = List.of(entrarText, holyHostingText, ajustesText, salirText);
        List<ButtonWidget.Builder> builders = List.of(entrarButton, holyHostingButton, ajustesButton, salirButton);

        applyLayout(config.layoutVersion, builders, buttonTexts);

        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.fadeInStart == 0L) {
            this.fadeInStart = Util.getMeasuringTimeMs();
        }

        MenuScreenVideo.render(context, this.width, this.height);
        super.render(context, mouseX, mouseY, delta);

        ci.cancel();
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        MenuScreenVideo.stop();
    }

    /**
     * ✅ REFACTORIZADO: Mi nuevo gestor de diseños que usa posicionamiento proporcional.
     * Ahora los botones se calculan en base a porcentajes de la pantalla,
     * asegurando que se vean bien en cualquier resolución y aspect ratio.
     * @param version La versión del layout (1-10).
     * @param builders Los constructores de los botones a posicionar.
     * @param buttonTexts Los textos de los botones para calcular el ancho inicial.
     */
    private void applyLayout(int version, List<ButtonWidget.Builder> builders, List<Text> buttonTexts) {
        // --- Cálculo de Tamaño Inteligente (CORREGIDO) ---
        int maxWidth = 0;
        for (Text text : buttonTexts) {
            maxWidth = Math.max(maxWidth, this.textRenderer.getWidth(text));
        }

        // El ancho base es el texto más un padding más ajustado.
        int baseButtonWidth = maxWidth + 20; // ❗ Mi cambio: Reduje el padding de 30 a 20.
        int buttonHeight = 20;

        // Limito el ancho para que no sea ni muy chico ni excesivamente grande.
        // ❗ Mi cambio: Reduje el mínimo a 100px y el máximo a 1/5 del ancho de la pantalla.
        int buttonWidth = MathHelper.clamp(baseButtonWidth, 100, this.width / 5);

        // --- Posicionamiento Proporcional ---
        int horizontalPadding = (int) (this.width * 0.03);
        int verticalPadding = (int) (this.height * 0.04);
        int spacing = 5;

        for (int i = 0; i < builders.size(); i++) {
            ButtonWidget.Builder builder = builders.get(i);
            int x = 0, y = 0;

            switch (version) {
                case 2: // Fila horizontal inferior
                    int totalWidth_v2 = (builders.size() * buttonWidth) + ((builders.size() - 1) * spacing);
                    int startX_v2 = (this.width - totalWidth_v2) / 2;
                    y = this.height - buttonHeight - verticalPadding;
                    x = startX_v2 + (i * (buttonWidth + spacing));
                    break;

                case 3: // Fila vertical izquierda
                    int totalHeight_v3 = (builders.size() * buttonHeight) + ((builders.size() - 1) * spacing);
                    int startY_v3 = (this.height - totalHeight_v3) / 2;
                    x = horizontalPadding;
                    y = startY_v3 + (i * (buttonHeight + spacing));
                    break;

                case 4: // Grid 2x2 centrado
                    int gridWidth = (2 * buttonWidth) + spacing;
                    int gridHeight = (2 * buttonHeight) + spacing;
                    int gridStartX = (this.width - gridWidth) / 2;
                    int gridStartY = (this.height - gridHeight) / 2;
                    int col_v4 = i % 2;
                    int row_v4 = i / 2;
                    x = gridStartX + (col_v4 * (buttonWidth + spacing));
                    y = gridStartY + (row_v4 * (buttonHeight + spacing));
                    break;

                case 5: // Fila horizontal superior
                    int totalWidth_v5 = (builders.size() * buttonWidth) + ((builders.size() - 1) * spacing);
                    int startX_v5 = (this.width - totalWidth_v5) / 2;
                    y = verticalPadding;
                    x = startX_v5 + (i * (buttonWidth + spacing));
                    break;

                case 6: // Fila vertical derecha
                    int totalHeight_v6 = (builders.size() * buttonHeight) + ((builders.size() - 1) * spacing);
                    int startY_v6 = (this.height - totalHeight_v6) / 2;
                    x = this.width - buttonWidth - horizontalPadding;
                    y = startY_v6 + (i * (buttonHeight + spacing));
                    break;

                case 7: // En las 4 esquinas
                    switch (i) {
                        case 0 -> { x = horizontalPadding; y = verticalPadding; }
                        case 1 -> { x = this.width - buttonWidth - horizontalPadding; y = verticalPadding; }
                        case 2 -> { x = horizontalPadding; y = this.height - buttonHeight - verticalPadding; }
                        case 3 -> { x = this.width - buttonWidth - horizontalPadding; y = this.height - buttonHeight - verticalPadding; }
                    }
                    break;

                case 8: // Columnas divididas
                    int yOffset_v8 = (this.height / 2) - buttonHeight - (spacing / 2);
                    if (i < 2) {
                        x = horizontalPadding + 20;
                        y = yOffset_v8 + (i * (buttonHeight + spacing));
                    } else {
                        x = this.width - buttonWidth - horizontalPadding - 20;
                        y = yOffset_v8 + ((i - 2) * (buttonHeight + spacing));
                    }
                    break;

                case 9: // Fila horizontal centrada
                    int totalWidth_v9 = (builders.size() * buttonWidth) + ((builders.size() - 1) * spacing);
                    int startX_v9 = (this.width - totalWidth_v9) / 2;
                    y = (this.height - buttonHeight) / 2;
                    x = startX_v9 + (i * (buttonWidth + spacing));
                    break;

                case 10: // Pila vertical centrada y espaciada
                    int wideSpacing = spacing * 4;
                    int totalHeight_v10 = (builders.size() * buttonHeight) + ((builders.size() - 1) * wideSpacing);
                    int startY_v10 = (this.height - totalHeight_v10) / 2;
                    x = (this.width - buttonWidth) / 2;
                    y = startY_v10 + (i * (wideSpacing + buttonHeight));
                    break;

                case 1: // Pila vertical centrada (default)
                default:
                    int totalHeight_v1 = (builders.size() * buttonHeight) + ((builders.size() - 1) * spacing);
                    int startY_v1 = (this.height - totalHeight_v1) / 2 + (int)(this.height * 0.1);
                    x = (this.width - buttonWidth) / 2;
                    y = startY_v1 + (i * (buttonHeight + spacing));
                    break;
            }

            this.addDrawableChild(builder.dimensions(x, y, buttonWidth, buttonHeight).build());
        }
    }
}