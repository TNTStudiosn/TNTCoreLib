// Ubicación: src/main/java/com/TNTStudios/tntcorelib/modulo/voicechat/VoiceChatAddon.java
package com.TNTStudios.tntcorelib.modulo.voicechat;

import com.TNTStudios.tntcorelib.Tntcorelib;
// ✅ ELIMINADO: Ya no necesito el CommandRegistrationCallback aquí.
// import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;

/**
 * Mi punto de entrada para que PlasmoVoice reconozca este módulo como un addon.
 * Utilizo la anotación @Addon para describirlo y @InjectPlasmoVoice para recibir la
 * instancia de la API principal, que es la forma correcta de integrarme.
 */
@Addon(
        id = "tntcorelib-voice", // ID único para mi addon de voz
        name = "TNTCoreLib Voice",
        version = "1.0.0",
        authors = {"TNTStudios"}
)
public class VoiceChatAddon implements AddonInitializer {

    // PlasmoVoice inyectará aquí su instancia de API del servidor. ¡Magia!
    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;

    /**
     * Este método se llama cuando PlasmoVoice carga mi addon.
     * Es el lugar perfecto para inicializar todo lo relacionado con el chat de voz.
     */
    @Override
    public void onAddonInitialize() {
        // 1. Creo mi manager, pasándole la instancia de la API que me inyectaron.
        VoiceChatManager voiceChatManager = new VoiceChatManager(voiceServer);

        // 2. Establezco la instancia de mi API en la clase principal para que sea accesible globalmente.
        Tntcorelib.setVoiceChatApi(voiceChatManager);

        // 3. ✅ ELIMINADO: El registro del comando se movió a Tntcorelib.onInitialize()
        // para asegurar que se ejecute en el momento correcto del ciclo de vida de Fabric.

        System.out.println("[TNTCoreLib] Módulo de Chat de Voz cargado como addon de PlasmoVoice.");
    }
}