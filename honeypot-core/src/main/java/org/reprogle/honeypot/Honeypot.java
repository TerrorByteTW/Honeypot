package org.reprogle.honeypot;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.CommandManager;
import org.reprogle.honeypot.events.*;
import org.reprogle.honeypot.gui.GUI;
import org.reprogle.honeypot.storagemanager.CacheManager;
import org.reprogle.honeypot.utils.GhostHoneypotFixer;
import org.reprogle.honeypot.utils.GriefPreventionUtil;
import org.reprogle.honeypot.utils.HoneypotConfigManager;
import org.reprogle.honeypot.utils.HoneypotLogger;
import org.reprogle.honeypot.utils.HoneypotUpdateChecker;
import org.reprogle.honeypot.utils.WorldGuardUtil;

import net.milkbowl.vault.permission.Permission;

public final class Honeypot extends JavaPlugin {

    private static Honeypot plugin;

    private static GUI gui;

    private static HoneypotLogger logger;

    private static Permission perms = null;

    private static WorldGuardUtil wgu = null;

    private static GriefPreventionUtil gpu = null;

    /**
     * Set up WorldGuard. This must be done in onLoad() due to how WorldGuard
     * registers flags.
     */
    @Override
    @SuppressWarnings("java:S2696")
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            wgu = new WorldGuardUtil();
            wgu.setupWorldGuard();
        }
    }

    /**
     * Enable method called by Bukkit. This is a little messy due to all the setup
     * it has to do
     */
    @Override
    @SuppressWarnings({ "unused", "java:S2696" })
    public void onEnable() {
        // Variables and stuff
        plugin = this;
        gui = new GUI(this);
        logger = new HoneypotLogger();

        // Create/load configuration files
        HoneypotConfigManager.setupConfig(this);

        // Setup Vault (This is a requirement!)
        if (!setupPermissions()) {
            getLogger().severe(
                    CommandFeedback.getChatPrefix() + ChatColor.RED + " Disabled due to Vault not being installed");
            logger.log(
                    "Disabling due to Vault not being installed. Please download here: https://www.spigotmc.org/resources/vault.34315/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register GriefPrevention (This could technically be a static function but
        // it's not due to the abstraction API)
        if (getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            gpu = new GriefPreventionUtil();
        }

        int pluginId = 15425;
        Metrics metrics = new Metrics(this, pluginId);

        // Start the GhostHoneypotFixer
        if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("ghost-honeypot-checker.enable"))) {
            getLogger().info(
                    "Starting the ghost checker task! If you need to disable this, update the config and restart the server");
            GhostHoneypotFixer.startTask();
        }

        // Set up listeners and command executor
        ListenerSetup.setupListeners(this);
        getCommand("honeypot").setExecutor(new CommandManager());
        logger.log("Loaded plugin");

        // Output the "splash" message
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "\n" +
                " _____                         _\n" +
                "|  |  |___ ___ ___ _ _ ___ ___| |_\n" +
                "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD +
                "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD +
                "                  |___|_|");

        // Output the version check message
        if (!versionCheck()) {
            getServer().getLogger().warning(
                    "Honeypot is not guaranteed to support this version of Spigot. We won't prevent you from using it, but some newer blocks (If any) may exhibit unusual behavior!");
        }

        // Check for any updates
        new HoneypotUpdateChecker(this, "https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/version.txt")
                .getVersion(latest -> {

                    if (Integer.parseInt(latest.replace(".", "")) > Integer
                            .parseInt(this.getDescription().getVersion().replace(".", ""))) {
                        getServer().getConsoleSender().sendMessage(CommandFeedback.getChatPrefix() + ChatColor.RED
                                + " There is a new update available: " + latest
                                + ". Please download for the latest features and security updates!");
                    } else {
                        getServer().getConsoleSender().sendMessage(CommandFeedback.getChatPrefix() + ChatColor.GREEN
                                + " You are on the latest version of Honeypot!");
                    }
                });
    }

    /**
     * Disable method called by Bukkit
     */
    @Override
    public void onDisable() {
        getLogger().info("Stopping the ghost checker task");
        GhostHoneypotFixer.cancelTask();
        CacheManager.clearCache();
        logger.log("Shut down plugin");
        getLogger().info("Successfully shutdown Honeypot. Bye for now!");
    }

    /**
     * Sets up the Permission hook for vault
     */
    @SuppressWarnings("java:S2696")
    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * Check the version of Spigot we're running on. Current supported version is
     * 1.17 - 1.19.2
     * 
     * @return True if the version is supported, false if not
     */
    public static boolean versionCheck() {
        String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        int majorVer = Integer.parseInt(split[0]);
        int minorVer = Integer.parseInt(split[1]);
        int revisionVer = split.length > 2 ? Integer.parseInt(split[2]) : 0;

        // Return true if between 1.17 & 1.19.2
        // TODO - Update for version 1.20
        return (majorVer == 1 && minorVer >= 17) && (majorVer == 1 && minorVer <= 19 && revisionVer <= 2);
    }

    /*
     * All the functions below are getter functinos
     * 
     * These simply return objects to prevent static keyword abuse
     */

    /**
     * Returns the plugin variable for use in other classes to get things such as
     * the logger
     *
     * @return plugin
     */
    public static Honeypot getPlugin() {
        return plugin;
    }

    /**
     * Returns the permission object for Vault
     * 
     * @return Vault {@link Permission}
     */
    public static Permission getPermissions() {
        return perms;
    }

    /**
     * Returns the GUI object of the plugin for GUI creation
     * 
     * @return {@link GUI}
     */
    public static GUI getGUI() {
        return gui;
    }

    /**
     * Gets the Honeypot logger
     * 
     * @return {@link HoneypotLogger}
     */
    public static HoneypotLogger getHoneypotLogger() {
        return logger;
    }

    /**
     * Retrieve the WorldGuard Util helper
     */
    public static WorldGuardUtil getWorldGuardUtil() {
        return wgu;
    }

    /**
     * Retrieve the GriefPrevention Util helper
     */
    public static GriefPreventionUtil getGriefPreventionUtil() {
        return gpu;
    }
}