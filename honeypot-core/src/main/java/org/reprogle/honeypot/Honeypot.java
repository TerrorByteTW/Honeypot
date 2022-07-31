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
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.utils.GhostHoneypotFixer;
import org.reprogle.honeypot.utils.GriefPreventionUtil;
import org.reprogle.honeypot.utils.HoneypotLogger;
import org.reprogle.honeypot.utils.WorldGuardUtil;

import net.milkbowl.vault.permission.Permission;

public final class Honeypot extends JavaPlugin {

    private static Honeypot plugin;

    private static GUI gui;

    private static HoneypotBlockManager hbm;

    private static HoneypotPlayerManager hpm;

    private static boolean testing = false;

    private static Permission perms = null;

    private static HoneypotLogger logger;

    private static WorldGuardUtil wgu = null;

    private static GriefPreventionUtil gpu = null;

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

    /**
     * Enable method called by Bukkit
     */
    @Override
    @SuppressWarnings({ "unused", "java:S2696" })
    public void onEnable() {
        // Variables and stuff
        plugin = this;
        gui = new GUI(this);
        logger = new HoneypotLogger();
        hbm = new HoneypotBlockManager();
        hpm = new HoneypotPlayerManager();

        // Create/load configuration files
        HoneypotConfigManager.setupConfig(this);

        // Setup Vault (This is a requirement!)
        if (!setupPermissions() && !testing) {
            getLogger().severe(
                    ConfigColorManager.getChatPrefix() + ChatColor.RED + " Disabled due to Vault not being installed");
            logger.log("Disabling due to Vault not being installed. Please download here: https://www.spigotmc.org/resources/vault.34315/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } 

        // Register GriefPrevention (This could technically be a static function but it's not due to the abstraction API)
        if (getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            gpu = new GriefPreventionUtil();
        }
        
        //Set up bStats
        if (Boolean.FALSE.equals(testing)) {
            int pluginId = 15425;
            Metrics metrics = new Metrics(this, pluginId);
        }

        // Start the GhostHoneypotFixer
        if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("ghost-honeypot-checker.enable"))) {
            getLogger().info("Starting the ghost checker task! If you need to disable this, update the config and restart the server");
            GhostHoneypotFixer.startTask();
        }

        // Set up listeners and command executor
        ListenerSetup.setupListeners(this);
        getCommand("honeypot").setExecutor(new CommandManager());
        logger.log("Loaded plugin");

        // Output the "splash" message
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

    /**
     * Set up WorldGuard. This must be done in onLoad() due to how WorldGuard registers flags.
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
     * Disable method called by Bukkit
     */
    @Override
    public void onDisable() {
        getLogger().info("Stopping the ghost checker task");
        GhostHoneypotFixer.cancelTask();
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
     * Checks if the plugin is running a Mock
     * 
     * @return Yes if mocking, no if running on a server
     */
    public static boolean getTesting() {
        return testing;
    }

    /**
     * Gets the HoneypotBlockManager object
     * 
     * @return {@link HoneypotBlockManager}
     */
    public static HoneypotBlockManager getHBM() {
        return hbm;
    }

    /**
     * Gets the HoneypotPlayerManager object
     * 
     * @return {@link HoneypotPlayerManager}
     */
    public static HoneypotPlayerManager getHPM() {
        return hpm;
    }

    /**
     * Gets the Honeypot logger
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