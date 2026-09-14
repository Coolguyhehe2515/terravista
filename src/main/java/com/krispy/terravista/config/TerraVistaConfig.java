package com.krispy.terravista.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TerraVistaConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("terravista.json");

    public boolean enabled = true;

    public int nearDistance = 128;

    public int lodDistance = 1024;

    public int renderDistance = 32;

    public int sampleStep = 4;

    public int maxChunksPerFrame = 2;

    private static TerraVistaConfig INSTANCE;

    public static TerraVistaConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }

        return INSTANCE;
    }

    public static void loadConfig() {
        INSTANCE = load();
    }

    private static TerraVistaConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            TerraVistaConfig config = new TerraVistaConfig();
            save(config);
            return config;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            TerraVistaConfig config = GSON.fromJson(json, TerraVistaConfig.class);

            if (config == null) {
                config = new TerraVistaConfig();
            }

            return config;
        } catch (IOException | RuntimeException e) {
            TerraVistaConfig config = new TerraVistaConfig();
            save(config);
            return config;
        }
    }

    public static void save() {
        save(get());
    }

    private static void save(TerraVistaConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException ignored) {
        }
    }
}
