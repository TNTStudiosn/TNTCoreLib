// Ubicación: src/main/java/com/TNTStudios/tntcorelib/Tntcorelib.java
package com.TNTStudios.tntcorelib;

import com.TNTStudios.tntcorelib.api.custommodels.CustomModelsApi;
import com.TNTStudios.tntcorelib.api.tablist.TablistApi;
import com.TNTStudios.tntcorelib.modulo.custommodels.CustomModelsHandler;
import com.TNTStudios.tntcorelib.modulo.tablist.TablistManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Tntcorelib implements ModInitializer {

    private static TablistApi tablistApiInstance;
    // ✅ NUEVO: Añado la instancia para la API de modelos personalizados.
    private static CustomModelsApi customModelsApiInstance;

    @Override
    public void onInitialize() {
        // ✅ CORREGIDO: Registro los comandos aquí, en el punto de entrada principal del mod.
        // Esto asegura que se registren durante la fase correcta de inicialización de Fabric,
        // antes de que el servidor empiece a cargar.
        CustomModelsHandler.registerCommands();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Inicializo mis managers cuando el servidor arranca.
            tablistApiInstance = new TablistManager(server);

            // ✅ CORREGIDO: Ahora llamo al método específico para inicializar el manager,
            // que necesita la instancia del servidor.
            CustomModelsHandler.initializeManager(server);
            customModelsApiInstance = CustomModelsHandler.getManager();
        });
    }

    /**
     * Mi punto de acceso público y estático a la API del Tablist.
     * (...)
     */
    public static TablistApi getTablistApi() {
        if (tablistApiInstance == null) {
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
     * ✅ NUEVO: Mi punto de acceso a la API de Modelos Personalizados.
     * (...)
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
}