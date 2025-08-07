// Ubicación: src/main/java/com/TNTStudios/tntcorelib/Tntcorelib.java
package com.TNTStudios.tntcorelib;

import com.TNTStudios.tntcorelib.api.custommodels.CustomModelsApi;
import com.TNTStudios.tntcorelib.api.playerstats.PlayerStatsApi;
import com.TNTStudios.tntcorelib.api.tablist.TablistApi;
import com.TNTStudios.tntcorelib.api.timer.TimerApi;
import com.TNTStudios.tntcorelib.api.voicechat.VoiceChatApi;
import com.TNTStudios.tntcorelib.api.tntalert.TNTAlertApi;
import com.TNTStudios.tntcorelib.api.tntalert.AlertType;
import com.TNTStudios.tntcorelib.modulo.connectionmessages.ConnectionMessagesHandler;
import com.TNTStudios.tntcorelib.modulo.custommodels.CustomModelsHandler;
import com.TNTStudios.tntcorelib.modulo.playerstats.PlayerStatsHandler;
import com.TNTStudios.tntcorelib.modulo.tablist.TablistManager;
import com.TNTStudios.tntcorelib.modulo.timer.TimerHandler;
import com.TNTStudios.tntcorelib.modulo.voicechat.VoiceChatAddon;
import com.TNTStudios.tntcorelib.modulo.voicechat.VoiceChatCommand;
import com.TNTStudios.tntcorelib.modulo.tntalert.TNTAlertHandler;
import com.TNTStudios.tntcorelib.network.ModPackets;
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

    public static final String MOD_ID = "tntcorelib";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);
    private static TablistApi tablistApiInstance;
    private static CustomModelsApi customModelsApiInstance;
    private static VoiceChatApi voiceChatApiInstance;
    private static TimerApi timerApiInstance;
    private static TNTAlertApi tntAlertApiInstance;
    private static PlayerStatsApi playerStatsApiInstance; // Ya tenías la variable, perfecto.

    private final VoiceChatAddon voiceChatAddon = new VoiceChatAddon();

    @Override
    public void onInitialize() {
        ModPackets.register();

        // ✅ Inicializo mi nuevo módulo para los mensajes de conexión.
        ConnectionMessagesHandler.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new VoiceChatCommand().register(dispatcher);
        });

        if (isPlasmoVoiceLoaded()) {
            PlasmoVoiceServer.getAddonsLoader().load(this.voiceChatAddon);
        }

        CustomModelsHandler.registerCommands();
        TimerHandler.registerCommands();
        TNTAlertHandler.registerCommands();
        PlayerStatsHandler.registerCommands(); // Esto está bien, registra el comando.

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            tablistApiInstance = new TablistManager(server);
            CustomModelsHandler.initializeManager(server);
            customModelsApiInstance = CustomModelsHandler.getManager();
            TimerHandler.initialize(server);
            timerApiInstance = TimerHandler.getManager();
            TNTAlertHandler.initializeManager(server);
            tntAlertApiInstance = TNTAlertHandler.getManager();
            PlayerStatsHandler.initializeManager(server);
            playerStatsApiInstance = PlayerStatsHandler.getManager(); // Aquí se inicializa la instancia real.
        });
    }

    public static PlayerStatsApi getPlayerStatsApi() {
        if (playerStatsApiInstance == null) {
            return new PlayerStatsApi() {
                @Override public void pauseHealth(ServerPlayerEntity player) {}
                @Override public void resumeHealth(ServerPlayerEntity player) {}
                @Override public void setHealth(ServerPlayerEntity player, float health) {}
                @Override public void pauseFood(ServerPlayerEntity player) {}
                @Override public void resumeFood(ServerPlayerEntity player) {}
                @Override public void setFoodLevel(ServerPlayerEntity player, int foodLevel) {}
                @Override public void pauseHealth(Collection<ServerPlayerEntity> players) {}
                @Override public void resumeHealth(Collection<ServerPlayerEntity> players) {}
                @Override public void setHealth(Collection<ServerPlayerEntity> players, float health) {}
                @Override public void pauseFood(Collection<ServerPlayerEntity> players) {}
                @Override public void resumeFood(Collection<ServerPlayerEntity> players) {}
                @Override public void setFoodLevel(Collection<ServerPlayerEntity> players, int foodLevel) {}
                @Override public boolean isHealthPaused(ServerPlayerEntity player) { return false; }
                @Override public boolean isFoodPaused(ServerPlayerEntity player) { return false; }
            };
        }
        return playerStatsApiInstance;
    }

    public static TNTAlertApi getTNTAlertApi() {
        if (tntAlertApiInstance == null) {
            return new TNTAlertApi() {
                @Override
                public void showAlert(Collection<ServerPlayerEntity> players, AlertType type, String title, String subtitle) {}

                @Override
                public void showToAll(AlertType type, String title, String subtitle) {}
            };
        }
        return tntAlertApiInstance;
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
                @Override public boolean isVisibleTo(ServerPlayerEntity player) { return false; }
            };
        }
        return timerApiInstance;
    }

    public static TablistApi getTablistApi() {
        if (tablistApiInstance == null) {
            return new TablistApi() {
                @Override public void hidePlayer(ServerPlayerEntity playerToHide, ServerPlayerEntity observer) {}
                @Override public void showPlayer(ServerPlayerEntity playerToShow, ServerPlayerEntity observer) {}
                @Override public boolean isPlayerHidden(ServerPlayerEntity player, ServerPlayerEntity observer) { return false; }
                @Override public boolean isPlayerHidden(UUID playerUuid, UUID observerUuid) { return false; }
            };
        }
        return tablistApiInstance;
    }

    public static CustomModelsApi getCustomModelsApi() {
        if (customModelsApiInstance == null) {
            return new CustomModelsApi() {
                @Override public void setModel(ServerPlayerEntity player, String modelName) {}
                @Override public void setModel(Collection<ServerPlayerEntity> players, String modelName) {}
                @Override public void resetModel(ServerPlayerEntity player) {}
                @Override public void resetModel(Collection<ServerPlayerEntity> players) {}
                @Override public List<String> getAvailableModels() { return Collections.emptyList(); }
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
            return new VoiceChatApi() {
                private void sendDisabledMessage(ServerPlayerEntity player) {
                    player.sendMessage(Text.literal("§c[Error] El módulo de voz no está activo en el servidor."), false);
                }
                @Override public void mutePlayer(ServerPlayerEntity player) { sendDisabledMessage(player); }
                @Override public void unmutePlayer(ServerPlayerEntity player) { sendDisabledMessage(player); }
                @Override public void muteAllPlayers() { System.out.println("[TNTCoreLib] Se intentó ejecutar 'muteAllPlayers' pero el módulo de voz está inactivo."); }
                @Override public void unmuteAllPlayers() { System.out.println("[TNTCoreLib] Se intentó ejecutar 'unmuteAllPlayers' pero el módulo de voz está inactivo."); }
            };
        }
        return voiceChatApiInstance;
    }
}