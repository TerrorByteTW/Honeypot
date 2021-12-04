package me.terrorbyte.honeypot;

import me.terrorbyte.honeypot.commands.CommandManager;
import me.terrorbyte.honeypot.events.*;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;

public final class Honeypot extends JavaPlugin {

    private static String databaseType;

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

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + " _____                         _\n" +
        "|  |  |___ ___ ___ _ _ ___ ___| |_\n" +
        "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD +
        "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD +
        "                  |___|_|");

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

        setupConfig();
        databaseType = getConfig().getString("database");

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

    public void setupConfig(){
        getConfig().addDefault("blocks-broken-before-action-taken", 1);
        getConfig().addDefault("allow-player-destruction", true);
        getConfig().addDefault("allow-explode", true);
        getConfig().addDefault("allow-enderman", true);
        getConfig().addDefault("enable-container-actions", true);
        getConfig().addDefault("kick-reason", "Kicked for breaking honeypot blocks. Don't grief!");
        getConfig().addDefault("ban-reason", "You have been banned for breaking honeypot blocks");
        getConfig().addDefault("warn-message", "Please don't grief builds!");
        getConfig().addDefault("chat-prefix", "&b[Honeypot]");

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    //Return the plugin instance
    public static Honeypot getPlugin() {
        return plugin;
    }

    //Static plugin variable, private to Honeypot to prevent changes
    private static Honeypot plugin;

    //Retrieve the database type from config in order to decide which storage mediums we're going to use
    public static String getDatabase() {
        return switch (Objects.requireNonNull(databaseType)) {
            case "sqlite", "json" -> databaseType;
            default -> "sqlite";
        };
    }

}
