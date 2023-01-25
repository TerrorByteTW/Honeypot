package org.reprogle.honeypot.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings.OptionSorting;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HoneypotConfigManager extends JavaPlugin {

    private static YamlDocument config;

    private static YamlDocument guiConfig;

    private static YamlDocument honeypotsConfig;

    private static YamlDocument languageFile;

    /*
     * I know this method of listing supported languages isn't great, but it's the
     * only option I could think of *right now*. There is no method that I know of
     * that Spigot has that can list all the files within the *embedded* resource
     * folder. `plugin.getDataFolder()` returns the data folder
     * of the plugin located in the server's /plugins directory. Therefore, there
     * wasn't really a way to generate this list on the fly.
     * We can always brute force decoding the jar to get the files that way but
     * that's suuuuper icky.
     * (https://www.spigotmc.org/threads/getresources-function.226318/ if anyone
     * wants an example)
     */
    private static List<String> languages = Arrays.asList("en_US", "es_MX", "fr_FR", "ja_JP");

    /**
     * Sets up the plugin config and saves it to private variables for use later.
     * Will shut down the plugin if there are
     * any IOExceptions as these config files are non-negotiable in the function of
     * this plugin.
     * 
     * @param plugin The Honeypot Plugin object
     */
    @SuppressWarnings({ "java:S1192", "java:S2629" })
    public static void setupConfig(Plugin plugin) {

        plugin.getLogger().info("Attempting to load all plugin config files...");
        try {
            config = YamlDocument.create(new File(plugin.getDataFolder(), "config.yml"),
                    plugin.getResource("config.yml"), GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build());

            config.update();
            config.save();
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not create/load plugin config, disabling! Please alert the plugin author with the following info: "
                            + e);
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        try {
            guiConfig = YamlDocument.create(new File(plugin.getDataFolder(), "gui.yml"), plugin.getResource("gui.yml"),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build());

            guiConfig.update();
            guiConfig.save();
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not create/load GUI config, disabling! Please alert the plugin author with following info: "
                            + e);
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        try {
            honeypotsConfig = YamlDocument.create(new File(plugin.getDataFolder(), "honeypots.yml"),
                    plugin.getResource("honeypots.yml"),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.builder().setAutoUpdate(false).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            guiConfig.update();
            guiConfig.save();
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not create/load Honeypot config, disabling! Please alert the plugin author with following info: "
                            + e);
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        String language = config.getString("language");

        if (!(languages.contains(language))
                && Boolean.FALSE.equals(config.getBoolean("bypass-language-check"))) {
            plugin.getLogger().warning("Language is currently set to " + language
                    + ". This language is currently not supported, defaulting to en_US.");
            language = "en_US";
        }

        try {
            languageFile = YamlDocument.create(new File(new File(plugin.getDataFolder(), "lang"), language + ".yml"),
                    plugin.getResource("lang/" + language + ".yml"),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("language-version"))
                            .setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build());

            languageFile.update();
            languageFile.save();

            plugin.getLogger().info("Language set to: " + language);
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not load language file, disabling! Please alert the plugin author with the following info:"
                            + e);
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        plugin.getLogger().info("Successfully loaded all plugin config files!");

    }

    /**
     * Returns the plugin config object
     * 
     * @return The YamlDocument object
     */
    public static YamlDocument getPluginConfig() {
        return config;
    }

    /**
     * Returns the plugin GUI config object
     * 
     * @return The YamlDocument object
     */
    public static YamlDocument getGuiConfig() {
        return guiConfig;
    }

    /**
     * Returns the plugin Honeypots config object
     * 
     * @return The YamlDocument object
     */
    public static YamlDocument getHoneypotsConfig() {
        return honeypotsConfig;
    }

    public static YamlDocument getLanguageFile() {
        return languageFile;
    }

}
