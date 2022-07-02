package org.reprogle.honeypot;

import java.io.File;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.reprogle.honeypot.commands.CommandManager;
import org.reprogle.honeypot.events.*;
import org.reprogle.honeypot.gui.GUI;

import net.milkbowl.vault.permission.Permission;

public final class Honeypot extends JavaPlugin {

    private static Honeypot plugin;

    private static GUI gui;

    private static boolean testing = false;

    private static Permission perms = null;

    /**
     * Constructor for MockBukkit
     */
    public Honeypot() {

    }

    /**
     * Constructor for MockBukkit
     */
    @SuppressWarnings("java:S3010")
    protected Honeypot(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        testing = true;
    }

    /**
     * Returns the plugin variable for use in other classes to get things such as the logger
     *
     * @return plugin
     */
    public static Honeypot getPlugin() {
        return plugin;
    }

    @Override
    @SuppressWarnings({ "unused", "java:S2696" })
    public void onEnable() {
        plugin = this;
        gui = new GUI(this);

        if (!setupPermissions() && !testing) {
            getLogger().severe(
                    ConfigColorManager.getChatPrefix() + ChatColor.RED + " Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Boolean.FALSE.equals(testing)) {
            // Setup bStats
            int pluginId = 15425;
            Metrics metrics = new Metrics(this, pluginId);
        }

        // Create/load configuration files
        HoneypotConfigManager.setupConfig(this);

        // Set up listeners and command executor
        ListenerSetup.setupListeners(this);
        getCommand("honeypot").setExecutor(new CommandManager());

        // Output the "splash screen"
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "\n" + 
            " _____                         _\n"
          + "|  |  |___ ___ ___ _ _ ___ ___| |_\n" 
          + "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD + 
            "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD + 
            "                  |___|_|");

        // Check for any updates
        new HoneypotUpdateChecker(this, "https://raw.githubusercontent.com/TerrrorByte/Honeypot/master/version.txt")
                .getVersion(latest -> {

                    if (Integer.parseInt(latest.replace(".", "")) > Integer
                            .parseInt(this.getDescription().getVersion().replace(".", ""))) {
                        getServer().getConsoleSender()
                                .sendMessage(ConfigColorManager.getChatPrefix() + ChatColor.RED
                                        + " There is a new update available: " + latest
                                        + ". Please download for the latest features and security updates!");
                    }
                    else {
                        getServer().getConsoleSender().sendMessage(ConfigColorManager.getChatPrefix() + ChatColor.GREEN
                                + " You are on the latest version of Honeypot!");
                    }
                });
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully shutdown Honeypot. Bye for now!");
    }

    @SuppressWarnings("java:S2696")
    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static GUI getGUI() {
        return gui;
    }

    public static boolean getTesting() {
        return testing;
    }
}
