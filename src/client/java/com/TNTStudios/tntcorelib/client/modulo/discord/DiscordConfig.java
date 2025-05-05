package com.TNTStudios.tntcorelib.client.modulo.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DiscordConfig {

    public String appId = "55";
    public String details = "👾Servidor de Tanizen🐲";
    public String state = "🌌𝗛𝗢𝗦𝗧𝗘𝗔𝗗𝗢 𝗘𝗡 𝗛𝗢𝗟𝗬𝗛𝗢𝗦𝗧𝗜𝗡𝗚🌌";
    public String largeImageKey = "log";
    public String largeImageText = "🌟Sevidor de Subs🌟";
    public String smallImageKey = "icon";
    public String smallImageText = "🚀TNTStudios🚀";
    public String button1Label = "🔥 HolyHosting 🔥";
    public String button1Url = "https://www.holy.gg/";
    public String button2Label = "⚡ TNTStudios ⚡";
    public String button2Url = "https://x.com/TNTStudiosN/";

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "TNTCore/Discord/Config.json");

    public static DiscordConfig load() {
        if (!CONFIG_FILE.exists()) {
            DiscordConfig defaultConfig = new DiscordConfig();
            defaultConfig.save(); // crear archivo si no existe
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            return new Gson().fromJson(reader, DiscordConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new DiscordConfig(); // fallback a default si error
        }
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
