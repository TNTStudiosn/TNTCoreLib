package com.TNTStudios.tntcorelib.client.modulo.discord;

import com.hypherionmc.craterlib.core.rpcsdk.DiscordRichPresence;
import com.hypherionmc.craterlib.core.rpcsdk.DiscordRPC;
import com.hypherionmc.craterlib.core.rpcsdk.helpers.RPCButton;

public class DiscordPresenceHandler {

    private static boolean initialized = false;
    private static DiscordConfig config;

    @SuppressWarnings("removal")
    public static void init() {
        if (initialized) return;

        config = DiscordConfig.load();
        DiscordRPC.INSTANCE.Discord_Initialize(config.appId, null, true, null);
        updatePresence();
        initialized = true;
    }

    @SuppressWarnings("removal")
    public static void updatePresence() {
        if (config == null) return;

        DiscordRichPresence presence = new DiscordRichPresence();
        presence.details = config.details;
        presence.state = config.state;
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.largeImageKey = config.largeImageKey;
        presence.largeImageText = config.largeImageText;
        presence.smallImageKey = config.smallImageKey;
        presence.smallImageText = config.smallImageText;

        RPCButton button1 = RPCButton.create(config.button1Label, config.button1Url);
        RPCButton button2 = RPCButton.create(config.button2Label, config.button2Url);

        presence.button_label_1 = button1.getLabel();
        presence.button_url_1 = button1.getUrl();
        presence.button_label_2 = button2.getLabel();
        presence.button_url_2 = button2.getUrl();

        DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
    }

    @SuppressWarnings("removal")
    public static void tick() {
        DiscordRPC.INSTANCE.Discord_RunCallbacks();
    }

    @SuppressWarnings("removal")
    public static void shutdown() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
    }
}
