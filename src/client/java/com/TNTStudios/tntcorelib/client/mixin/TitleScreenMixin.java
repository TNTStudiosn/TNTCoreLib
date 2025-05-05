package com.TNTStudios.tntcorelib.client.mixin;

import com.TNTStudios.tntcorelib.client.modulo.custommenu.MenuScreenVideo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        MenuScreenVideo.tryInitVideo();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", shift = At.Shift.BEFORE))
    private void renderVideo(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MenuScreenVideo.render(
                ctx,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight()
        );
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        MenuScreenVideo.stop();
    }

}
