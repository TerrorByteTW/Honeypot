package me.terrorbyte.honeypot;

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

public class HoneypotConfigManager extends JavaPlugin {

    private static YamlDocument config;

    private static YamlDocument guiConfig;

    /**
     * Sets up the plugin config and saves it to private variables for use later. Will shut down the plugin if there are
     * any IOExceptions as these config files are non-negotiable in the function of this plugin.
     * 
     * @param plugin The Honeypot Plugin object
     */
    public static void setupConfig(Plugin plugin) {

        plugin.getLogger().info("Attempting to load plugin config...");
        try {
            config = YamlDocument.create(new File(plugin.getDataFolder(), "config.yml"),
                    plugin.getResource("config.yml"), GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build());

            config.update();
            config.save();
            plugin.getLogger().info("Plugin config successfully loaded/created!");
        }
        catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe(
                    "Could not create/load plugin config, disabling! Please alert the plugin author with the full stack trace above");
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        plugin.getLogger().info("Attempting to load GUI config...");
        try {
            guiConfig = YamlDocument.create(new File(plugin.getDataFolder(), "gui.yml"), plugin.getResource("gui.yml"),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build());

            guiConfig.update();
            guiConfig.save();
            plugin.getLogger().info("GUI config successfully loaded/created!");
        }
        catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe(
                    "Could not create/load GUI config, disabling! Please alert the plugin author with the full stack trace above");
            plugin.getPluginLoader().disablePlugin(plugin);
            e.printStackTrace();
        }

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

}
