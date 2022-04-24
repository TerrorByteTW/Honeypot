package me.terrorbyte.honeypot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.terrorbyte.honeypot.commands.CommandManager;
import me.terrorbyte.honeypot.events.*;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class Honeypot extends JavaPlugin {

    private static String databaseType;
    public static YamlDocument config;

    public static Honeypot getPlugin() {
        return plugin;
    }

    private static Honeypot plugin;

    //Retrieve the database type from config in order to decide which storage mediums we're going to use
    public static String getDatabase() {
        return switch (Objects.requireNonNull(databaseType)) {
            case "sqlite", "json" -> databaseType;
            default -> "sqlite";
        };
    }

    //On enable, register the block break event listener, register the command manager, and log to the console
    @Override
    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(new PlayerBreakEventListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionEventListener(), this);
        getServer().getPluginManager().registerEvents(new EntityChangeEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerContainerOpenListener(), this);
        getServer().getPluginManager().registerEvents(new PistonMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Objects.requireNonNull(getCommand("honeypot")).setExecutor(new CommandManager());
        //getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Enabled " + ChatColor.GOLD + "Honeypot" + ChatColor.AQUA + " anti-cheat honeypot plugin");

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD +
        " _____                         _\n" +
        "|  |  |___ ___ ___ _ _ ___ ___| |_\n" +
        "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD +
        "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD +
        "                  |___|_|");

        try {
            config = HoneypotConfigManager.setupConfig(YamlDocument.create(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build()));
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not create config, disabling! Please alert the plugin author with the full stack trace above");
            this.getPluginLoader().disablePlugin(this);
        }

        databaseType = config.getString("database");

        try {
            getLogger().info("Loading honeypot blocks...");
            HoneypotBlockStorageManager.loadHoneypotBlocks(plugin);

            getLogger().info("Loading honeypot players...");
            HoneypotPlayerStorageManager.loadHoneypotPlayers(plugin);

            getLogger().info("Successfully enabled");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not load honeypot blocks or players, disabling! Please alert the plugin author with the full stack trace above");
            this.getPluginLoader().disablePlugin(this);
        }

        new HoneypotUpdateChecker(this, "https://raw.githubusercontent.com/redstonefreak589/Honeypot/master/version.txt").getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "You are on the latest version of Honeypot!");
            } else {
                getServer().getConsoleSender().sendMessage(ChatColor.RED + "There is a new update available: " + version + ". Please download for the latest features and security updates!");
            }
        });
    }

    //On disable, do nothing, really
    @Override
    public void onDisable() {
        // Save any unsaved HoneypotBlocks for whatever reason

        try {
            getLogger().info("Saving honeypot blocks...");
            HoneypotBlockStorageManager.saveHoneypotBlocks();
            getLogger().info("Saving honeypot players...");
            HoneypotPlayerStorageManager.saveHoneypotPlayers();
            getLogger().info("Successfully shutdown Honeypot. Bye for !");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not save honeypot blocks or players. You may experience issues restarting this plugin. Please alert the plugin author with the full stack trace above");
        }
    }
}
