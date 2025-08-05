// Ubicación: src/main/java/com/TNTStudios/tntcorelib/Tntcorelib.java
package com.TNTStudios.tntcorelib;

import com.TNTStudios.tntcorelib.api.custommodels.CustomModelsApi;
import com.TNTStudios.tntcorelib.api.tablist.TablistApi;
import com.TNTStudios.tntcorelib.api.voicechat.VoiceChatApi;
import com.TNTStudios.tntcorelib.modulo.custommodels.CustomModelsHandler;
import com.TNTStudios.tntcorelib.modulo.tablist.TablistManager;
// ✅ NUEVO: Importo mi clase de comando y el callback de Fabric.
import com.TNTStudios.tntcorelib.modulo.voicechat.VoiceChatCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Tntcorelib implements ModInitializer {

    private static TablistApi tablistApiInstance;
    private static CustomModelsApi customModelsApiInstance;
    private static VoiceChatApi voiceChatApiInstance;

    @Override
    public void onInitialize() {
        // La inicialización del manager de voz la sigue gestionando el addon.
        CustomModelsHandler.registerCommands();

        // ✅ NUEVO: Registro el comando de voice chat aquí para asegurar que se haga
        // en el momento correcto del ciclo de vida de Fabric.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                VoiceChatCommand.register(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Inicializo mis managers cuando el servidor arranca.
            tablistApiInstance = new TablistManager(server);

            CustomModelsHandler.initializeManager(server);
            customModelsApiInstance = CustomModelsHandler.getManager();
        });
    }

    // Un setter para que mi addon pueda establecer la instancia de la API.
    public static void setVoiceChatApi(VoiceChatApi api) {
        if (voiceChatApiInstance == null) {
            voiceChatApiInstance = api;
        }
    }

    /**
     * Mi punto de acceso público y estático a la API del Tablist.
     */
    public static TablistApi getTablistApi() {
        if (tablistApiInstance == null) {
            // Devuelvo una implementación 'dummy' para evitar NullPointerExceptions si se llama antes de tiempo.
            return new TablistApi() {
                @Override
                public void hidePlayer(ServerPlayerEntity playerToHide, ServerPlayerEntity observer) {}
                @Override
                public void showPlayer(ServerPlayerEntity playerToShow, ServerPlayerEntity observer) {}
                @Override
                public boolean isPlayerHidden(ServerPlayerEntity player, ServerPlayerEntity observer) { return false; }
                @Override
                public boolean isPlayerHidden(UUID playerUuid, UUID observerUuid) { return false; }
            };
        }
        return tablistApiInstance;
    }

    /**
     * Mi punto de acceso a la API de Modelos Personalizados.
     */
    public static CustomModelsApi getCustomModelsApi() {
        if (customModelsApiInstance == null) {
            return new CustomModelsApi() {
                @Override
                public void setModel(ServerPlayerEntity player, String modelName) {}
                @Override
                public void setModel(Collection<ServerPlayerEntity> players, String modelName) {}
                @Override
                public void resetModel(ServerPlayerEntity player) {}
                @Override
                public void resetModel(Collection<ServerPlayerEntity> players) {}
                @Override
                public List<String> getAvailableModels() { return Collections.emptyList(); }
            };
        }
        return customModelsApiInstance;
    }

    /**
     * Mi punto de acceso a la API de Chat de Voz.
     */
    public static VoiceChatApi getVoiceChatApi() {
        if (voiceChatApiInstance == null) {
            // Implementación 'dummy' para seguridad.
            return new VoiceChatApi() {
                @Override
                public void mutePlayer(ServerPlayerEntity player) {}
                @Override
                public void unmutePlayer(ServerPlayerEntity player) {}
                @Override
                public void muteAllPlayers() {}
                @Override
                public void unmuteAllPlayers() {}
            };
        }
        return voiceChatApiInstance;
    }
}