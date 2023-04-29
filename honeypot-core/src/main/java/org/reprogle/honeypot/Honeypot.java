package org.reprogle.honeypot;

import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.CommandManager;
import org.reprogle.honeypot.events.ListenerSetup;
import org.reprogle.honeypot.gui.GUI;
import org.reprogle.honeypot.storagemanager.CacheManager;
import org.reprogle.honeypot.utils.*;

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
    @SuppressWarnings({"unused", "java:S2696"})
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

        // Register GriefPrevention (This could technically be a static function, but
        // it's not due to the abstraction API)
        if (getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            gpu = new GriefPreventionUtil();
        }

        // Initialize Bstats
        Metrics metrics = new Metrics(this, 15425);

        // Start the GhostHoneypotFixer
        if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("ghost-honeypot-checker.enable"))) {
            getLogger().info(
                    "Starting the ghost checker task! If you need to disable this, update the config and restart the server");
            GhostHoneypotFixer.startTask();
        }

        // Set up listeners and command executor
        ListenerSetup.setupListeners(this);
        //noinspection DataFlowIssue
        getCommand("honeypot").setExecutor(new CommandManager());
        logger.log("Loaded plugin");

        // Output the splash message
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "\n" +
                " _____                         _\n" +
                "|  |  |___ ___ ___ _ _ ___ ___| |_\n" +
                "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD +
                "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + this.getDescription().getVersion() + "\n" + ChatColor.GOLD +
                "                  |___|_|");

        // A small helper method to verify if the server version is supported by Honeypot. I've moved it to its own method because it's rather large
        checkIfServerSupported();

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
        perms = rsp != null ? rsp.getProvider() : null;
        return perms != null;
    }

    /**
     * Check the GitHub repo of the plugin to verify the version of Spigot we're running on is supported
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static void checkIfServerSupported() {
        String[] serverVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        int serverMajorVer = Integer.parseInt(serverVersion[0]);
        int serverMinorVer = Integer.parseInt(serverVersion[1]);
        int serverRevisionVer = serverVersion.length > 2 ? Integer.parseInt(serverVersion[2]) : 0;

        String pluginVersion = plugin.getDescription().getVersion();
        // Check for any updates
        new HoneypotSupportedVersions(plugin, pluginVersion)
                .getSupportedVersions(value -> {
                    // Get the least supported and most supported server versions for this version of Honeypot
                    String[] lowerVersion = value.split("-")[0].split("\\.");
                    String[] upperVersion = value.split("-")[1].split("\\.");

                    int lowerMajorVer = Integer.parseInt(lowerVersion[0]);
                    int lowerMinorVer = Integer.parseInt(lowerVersion[1]);
                    int lowerRevisionVer = lowerVersion.length > 2 ? Integer.parseInt(lowerVersion[2]) : 0;

                    int upperMajorVer = Integer.parseInt(upperVersion[1]);
                    int upperMinorVer = Integer.parseInt(upperVersion[1]);
                    int upperRevisionVer = lowerVersion.length > 2 ? Integer.parseInt(upperVersion[2]) : 0;

                    // Check if the version the server is running is within the bounds of the supported versions
                    // We are doing this check dynamically because it allows us to verify and disable version check messages without updating the plugin code
                    // This means if a minor MC version rolls out and doesn't affect functionality to the plugin, we can update it on the GitHub side and server admins will not see an error message
                    if ((serverMajorVer < lowerMajorVer || serverMajorVer > upperMajorVer) &&
                            (serverMinorVer < lowerMinorVer || serverMinorVer >= upperMinorVer) &&
                            (serverRevisionVer < lowerRevisionVer || serverRevisionVer > upperRevisionVer)) {
                        plugin.getServer().getLogger().warning(
                                "Honeypot is not guaranteed to support this version of Spigot. We won't prevent you from using it, but some newer blocks (If any) may exhibit unusual behavior!");
                        plugin.getServer().getLogger().warning(
                                "Honeypot " + pluginVersion + " supports server versions " + value);
                    }
                });

    }

    /*
     * All the functions below are getter functions
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