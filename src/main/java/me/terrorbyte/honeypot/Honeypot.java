package me.terrorbyte.honeypot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.terrorbyte.honeypot.commands.CommandManager;
import me.terrorbyte.honeypot.events.*;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Honeypot extends JavaPlugin {

    public static YamlDocument config;
    private static Honeypot plugin;

    public static Honeypot getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        try {
            config = HoneypotConfigManager.setupConfig(YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    getResource("config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build()));
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not create config, disabling! Please alert the plugin author with the full stack trace above");
            this.getPluginLoader().disablePlugin(this);
        }
        
        ListenerSetup.SetupListeners(this);
        getCommand("honeypot").setExecutor(new CommandManager());

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD +
        " _____                         _\n" +
        "|  |  |___ ___ ___ _ _ ___ ___| |_\n" +
        "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD +
        "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD +
        "                  |___|_|");

        new HoneypotUpdateChecker(this, "https://raw.githubusercontent.com/redstonefreak589/Honeypot/master/version.txt").getVersion(version -> {
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
