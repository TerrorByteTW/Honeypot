package me.terrorbyte.honeypot;

import me.terrorbyte.honeypot.commands.CommandManager;
import me.terrorbyte.honeypot.events.*;
import me.terrorbyte.honeypot.gui.GUI;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Honeypot extends JavaPlugin {

    private static Honeypot plugin;
    public static GUI gui;

    /**
     * Returns the plugin variable for use in other classes to get things such as the logger
     *
     * @return plugin
     */
    public static Honeypot getPlugin() {
        return plugin;
    }

    @Override
    @SuppressWarnings("unused")
    public void onEnable() {
        plugin = this;
        gui = new GUI(this);

        // Setup bStats
        int pluginId = 15425;
        Metrics metrics = new Metrics(this, pluginId);

        // Create/load configuration files
        HoneypotConfigManager.setupConfig(this);
        
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
