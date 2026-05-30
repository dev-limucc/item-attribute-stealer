package dev.limucc.itemattributestealer.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.limucc.itemattributestealer.ItemAttributeStealer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles loading and saving ModConfig to/from disk.
 *
 * Learning note: Minecraft ships Gson in its classpath so we can use it without
 * adding an extra dependency.  FabricLoader.getConfigDir() gives us the
 * per-instance .minecraft/config/ folder — standard place for mod configs.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("item_attribute_stealer.json");

    /** The in-memory config.  Always use ConfigManager.get() to read it. */
    private static ModConfig instance = new ModConfig();

    /** Returns the currently-loaded config.  Never null after load() is called. */
    public static ModConfig get() {
        return instance;
    }

    /**
     * Loads config from disk.  If the file doesn't exist the defaults in
     * ModConfig are used and a fresh file is written.
     *
     * Called once in ItemAttributeStealerClient.onInitializeClient().
     */
    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            ItemAttributeStealer.LOGGER.info("No config file found, writing defaults.");
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            instance = GSON.fromJson(reader, ModConfig.class);
            if (instance == null) instance = new ModConfig(); // guard: empty file
            ItemAttributeStealer.LOGGER.info("Config loaded from {}", CONFIG_PATH);
        } catch (IOException e) {
            ItemAttributeStealer.LOGGER.error("Failed to load config, using defaults.", e);
            instance = new ModConfig();
        }
    }

    /**
     * Saves the current in-memory config to disk.
     *
     * Called by the Cloth Config screen's save runnable (so changes persist
     * when the user closes the settings screen).
     */
    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(instance, writer);
            ItemAttributeStealer.LOGGER.info("Config saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            ItemAttributeStealer.LOGGER.error("Failed to save config.", e);
        }
    }
}
