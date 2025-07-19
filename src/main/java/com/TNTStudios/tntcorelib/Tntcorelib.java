package com.TNTStudios.tntcorelib;

import com.TNTStudios.tntcorelib.api.tablist.TablistApi;
import com.TNTStudios.tntcorelib.modulo.tablist.TablistManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class Tntcorelib implements ModInitializer {

    private static TablistApi tablistApiInstance;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            tablistApiInstance = new TablistManager(server);
        });
    }

    /**
     * Mi punto de acceso público y estático a la API del Tablist.
     * Cualquier mod puede llamar a Tntcorelib.getTablistApi() para obtener la instancia.
     * Si la API no está lista (servidor no iniciado), devuelvo una implementación vacía
     * para evitar NullPointerExceptions y garantizar la estabilidad.
     *
     * @return La instancia de la API del Tablist.
     */
    public static TablistApi getTablistApi() {
        if (tablistApiInstance == null) {
            // Este es mi "fallback" seguro. Si alguien llama a la API antes de tiempo,
            // no hará nada y no romperá el juego.
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
}