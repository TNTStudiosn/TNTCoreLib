// Ubicación: src/main/java/com/TNTStudios/tntcorelib/Tntcorelib.java
package com.TNTStudios.tntcorelib;

import com.TNTStudios.tntcorelib.api.custommodels.CustomModelsApi;
import com.TNTStudios.tntcorelib.api.tablist.TablistApi;
import com.TNTStudios.tntcorelib.api.timer.TimerApi; // ✅ NUEVO: Importo mi nueva API.
import com.TNTStudios.tntcorelib.api.voicechat.VoiceChatApi;
import com.TNTStudios.tntcorelib.modulo.custommodels.CustomModelsHandler;
import com.TNTStudios.tntcorelib.modulo.tablist.TablistManager;
import com.TNTStudios.tntcorelib.modulo.timer.TimerHandler; // ✅ NUEVO: Importo mi nuevo handler.
import com.TNTStudios.tntcorelib.modulo.voicechat.VoiceChatAddon;
import com.TNTStudios.tntcorelib.modulo.voicechat.VoiceChatCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import su.plo.voice.api.server.PlasmoVoiceServer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Tntcorelib implements ModInitializer {

    private static TablistApi tablistApiInstance;
    private static CustomModelsApi customModelsApiInstance;
    private static VoiceChatApi voiceChatApiInstance;
    private static TimerApi timerApiInstance; // ✅ NUEVO: Añado la instancia para mi API del timer.

    // Creo la instancia del addon para cargarla después.
    private final VoiceChatAddon voiceChatAddon = new VoiceChatAddon();

    @Override
    public void onInitialize() {
        // 1. Registro el comando aquí. Esto se ejecuta en el momento correcto.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new VoiceChatCommand().register(dispatcher);
        });

        // 2. Intento cargar el addon de PlasmoVoice.
        if (isPlasmoVoiceLoaded()) {
            PlasmoVoiceServer.getAddonsLoader().load(this.voiceChatAddon);
        }

        // El resto de inicializaciones siguen igual.
        CustomModelsHandler.registerCommands();
        TimerHandler.registerCommands(); // ✅ NUEVO: Registro los comandos de mi nuevo módulo.

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            tablistApiInstance = new TablistManager(server);
            CustomModelsHandler.initializeManager(server);
            customModelsApiInstance = CustomModelsHandler.getManager();
            TimerHandler.initialize(server); // ✅ NUEVO: Inicializo mi módulo de timer.
            timerApiInstance = TimerHandler.getManager(); // ✅ NUEVO: Asigno la implementación a mi API.
        });
    }

    public static TimerApi getTimerApi() {
        if (timerApiInstance == null) {
            return new TimerApi() {
                @Override public void start() {}
                @Override public void pause() {}
                @Override public void stop() {}
                @Override public void setTime(int seconds) {}
                @Override public void addTime(int seconds) {}
                @Override public int getTimeRemaining() { return 0; }
                @Override public boolean isRunning() { return false; }
                @Override public void styleBossBar(Consumer<BossBar> consumer) {}
                @Override public void setOnFinishAction(Runnable onFinish) {}
                @Override public void show(ServerPlayerEntity player) {}
                @Override public void hide(ServerPlayerEntity player) {}
                @Override public void showToAll() {}
                @Override public void hideFromAll() {}

                @Override
                public boolean isVisibleTo(ServerPlayerEntity player) {
                    return false;
                }
            };
        }
        return timerApiInstance;
    }


    // Los getters de las APIs no cambian.
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

    private boolean isPlasmoVoiceLoaded() {
        try {
            Class.forName("su.plo.voice.api.server.PlasmoVoiceServer");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("[TNTCoreLib] PlasmoVoice no encontrado. El módulo de chat de voz será desactivado.");
            return false;
        }
    }

    public static void setVoiceChatApi(VoiceChatApi api) {
        if (voiceChatApiInstance == null) {
            voiceChatApiInstance = api;
        }
    }

    public static VoiceChatApi getVoiceChatApi() {
        if (voiceChatApiInstance == null) {
            // Ahora mi implementación "dummy" es más inteligente.
            // Avisa al jugador si intenta usar la función sin tener PlasmoVoice.
            return new VoiceChatApi() {
                private void sendDisabledMessage(ServerPlayerEntity player) {
                    player.sendMessage(Text.literal("§c[Error] El módulo de voz no está activo en el servidor."), false);
                }

                @Override
                public void mutePlayer(ServerPlayerEntity player) {
                    sendDisabledMessage(player);
                }

                @Override
                public void unmutePlayer(ServerPlayerEntity player) {
                    sendDisabledMessage(player);
                }

                @Override
                public void muteAllPlayers() {
                    // No hay un jugador específico a quién notificar, así que lo logueo en consola.
                    System.out.println("[TNTCoreLib] Se intentó ejecutar 'muteAllPlayers' pero el módulo de voz está inactivo.");
                }

                @Override
                public void unmuteAllPlayers() {
                    System.out.println("[TNTCoreLib] Se intentó ejecutar 'unmuteAllPlayers' pero el módulo de voz está inactivo.");
                }
            };
        }
        return voiceChatApiInstance;
    }
}