// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/voicechat/VoiceChatManager.java
package com.TNTStudios.tntcorelib.modulo.voicechat;

import com.TNTStudios.tntcorelib.api.voicechat.VoiceChatApi;
import net.minecraft.server.network.ServerPlayerEntity;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.mute.MuteManager;

/**
 * Mi implementación de la VoiceChatApi.
 * Aquí es donde hago el trabajo pesado, comunicándome con el MuteManager de PlasmoVoice
 * para ejecutar las acciones de muteo y desmuteo.
 */
public class VoiceChatManager implements VoiceChatApi {

    private final PlasmoVoiceServer voiceServer;
    private final MuteManager muteManager;

    public VoiceChatManager(PlasmoVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
        // Obtengo el MuteManager de la instancia de PlasmoVoice. Es mi punto de entrada principal.
        this.muteManager = voiceServer.getMuteManager();
    }

    @Override
    public void mutePlayer(ServerPlayerEntity player) {
        // La API de PlasmoVoice me permite hacer mutes permanentes si no especifico duración.
        // Como no quiero una razón, paso null. El `mutedById` también es null porque es una acción del sistema.
        muteManager.mute(player.getUuid(), null, 0, null, null, false);
    }

    @Override
    public void unmutePlayer(ServerPlayerEntity player) {
        muteManager.unmute(player.getUuid(), false);
    }

    @Override
    public void muteAllPlayers() {
        // Corregido: Ahora obtengo el PlayerManager directamente desde la instancia de PlasmoVoiceServer.
        // Esto me da acceso a todos los jugadores que están usando el chat de voz.
        voiceServer.getPlayerManager().getPlayers().forEach(player -> {
            // Aplico el mute directamente usando el UUID del jugador, es más eficiente.
            muteManager.mute(player.getInstance().getUuid(), null, 0, null, null, false);
        });
    }

    @Override
    public void unmuteAllPlayers() {
        // Corregido: Igual que en muteAllPlayers, uso el PlayerManager de PlasmoVoice.
        voiceServer.getPlayerManager().getPlayers().forEach(player -> {
            muteManager.unmute(player.getInstance().getUuid(), false);
        });
    }
}