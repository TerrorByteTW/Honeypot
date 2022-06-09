package me.terrorbyte.honeypot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings.OptionSorting;
import me.terrorbyte.honeypot.commands.CommandManager;
import me.terrorbyte.honeypot.events.*;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Honeypot extends JavaPlugin {

    public static YamlDocument config;
    public static YamlDocument guiConfig;
    private static Honeypot plugin;

    public static Honeypot getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Create/load configuration files
        getLogger().info("Loading plugin config file...");
        try {
            config = HoneypotConfigManager.setupConfig(YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    getResource("config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build()));
            getLogger().info("Plugin config successfully loaded/created!");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not create plugin config, disabling! Please alert the plugin author with the full stack trace above");
            this.getPluginLoader().disablePlugin(this);
        }

        // Create/load GUI configuration file
        getLogger().info("Loading GUI config file...");
        try {
            guiConfig = HoneypotConfigManager.setupConfig(YamlDocument.create(new File(getDataFolder(), "gui.yml"),
                    getResource("gui.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).setOptionSorting(OptionSorting.SORT_BY_DEFAULTS).build()));
            getLogger().info("GUI config file successfully loaded/created!");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not create GUI config, disabling! Please alert the plugin author with the full stack trace above");
            this.getPluginLoader().disablePlugin(this);
        }
        
        //Set up listeners and command executor
        ListenerSetup.SetupListeners(this);
        getCommand("honeypot").setExecutor(new CommandManager());

        // Output the "splash screen"
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD +
        " _____                         _\n" +
        "|  |  |___ ___ ___ _ _ ___ ___| |_\n" +
        "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD +
        "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD +
        "                  |___|_|");

        // Check for any updates
        new HoneypotUpdateChecker(this, "https://raw.githubusercontent.com/TerrrorByte/Honeypot/master/version.txt").getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "You are on the latest version of Honeypot!");
            } else {
                getServer().getConsoleSender().sendMessage(ChatColor.RED + "There is a new update available: " + version + ". Please download for the latest features and security updates!");
            }
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully shutdown Honeypot. Bye for now!");
    }
}
