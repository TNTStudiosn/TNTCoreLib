// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/voicechat/VoiceChatAddon.java
package com.TNTStudios.tntcorelib.modulo.voicechat;

import com.TNTStudios.tntcorelib.Tntcorelib;
import com.TNTStudios.tntcorelib.api.voicechat.VoiceChatApi;
// Ya no necesito CommandRegistrationCallback aquí
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Addon(
        id = "tntcorelib-voice",
        name = "TNTCoreLib Voice",
        version = "1.0.0",
        authors = {"TNTStudios"}
)
public class VoiceChatAddon implements AddonInitializer {

    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;

    @Override
    public void onAddonInitialize() {
        // 1. Creo mi manager, que es la implementación concreta de mi API.
        VoiceChatApi voiceChatApi = new VoiceChatManager(voiceServer);

        // 2. Comunico la instancia de la API a mi clase principal.
        // Esto reemplazará la implementación "dummy" por la real.
        Tntcorelib.setVoiceChatApi(voiceChatApi);

        // ¡Ya no registro los comandos desde aquí! Esa responsabilidad ahora es de la clase principal.

        System.out.println("[TNTCoreLib] Módulo de Chat de Voz cargado e inicializado.");
    }
}