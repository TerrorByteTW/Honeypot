/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot;

import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.CommandManager;
import org.reprogle.honeypot.common.events.ListenerSetup;
import org.reprogle.honeypot.common.gui.GUI;
import org.reprogle.honeypot.common.providers.BehaviorProcessor;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.BehaviorRegistry;
import org.reprogle.honeypot.common.providers.included.Ban;
import org.reprogle.honeypot.common.providers.included.Kick;
import org.reprogle.honeypot.common.providers.included.Notify;
import org.reprogle.honeypot.common.providers.included.Warn;
import org.reprogle.honeypot.common.storagemanager.CacheManager;
import org.reprogle.honeypot.common.utils.*;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.PlaceholderAPIExpansion;

@SuppressWarnings({ "deprecation", "java:S1444", "java:S1104" })
public final class Honeypot extends JavaPlugin {

	public static Honeypot plugin;

	private static GUI gui;

	private static HoneypotLogger logger;

	private static Permission perms = null;

	private static GhostHoneypotFixer ghf = null;

	private static BehaviorRegistry registry = new BehaviorRegistry();

	// Sonarlint doesn't know what it's talking about. We need to assign
	// BehaviorProcessor in the onLoad() method, so it can't be final
	@SuppressWarnings({ "java:S1444" })
	public static BehaviorProcessor processor = null;

	private final BehaviorProvider[] builtInProviders = new BehaviorProvider[] { new Ban(), new Kick(), new Warn(),
			new Notify() };

	/**
	 * Set up WorldGuard. This must be done in onLoad() due to how WorldGuard registers flags.
	 */
	@Override
	@SuppressWarnings("java:S2696")
	public void onLoad() {
		AdapterManager.onLoadAdapters(getServer());

		registry = new BehaviorRegistry();

		for (BehaviorProvider behavior : builtInProviders) {
			registry.register(behavior);
		}

		processor = new BehaviorProcessor();

	}

	/**
	 * Enable method called by Bukkit. This is a little messy due to all the setup it has to do
	 */
	@Override
	@SuppressWarnings({ "unused", "java:S2696" })
	public void onEnable() {
		plugin = this;
		gui = new GUI(this);
		logger = new HoneypotLogger();
		registry.setInitialized(true);

		HoneypotConfigManager.setupConfig(this);

		getHoneypotLogger().info("Successfully registered " + registry.size()
				+ " behavior providers. Further registrations are now locked.");

		// Load everything necessary for the plugin to work
		Metrics metrics = new Metrics(this, 15425);
		ListenerSetup.setupListeners(this);

		ghf = new GhostHoneypotFixer();

		// Setup Vault
		if (!setupPermissions()) {
			getHoneypotLogger().warning(CommandFeedback.getChatPrefix() + ChatColor.RED
					+ " Vault is not installed, some features won't work. Some features won't work. Please download here: https://www.spigotmc.org/resources/vault.34315/");
		}

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderAPIExpansion(this).register();
		}

		// Register remaining adapters
		AdapterManager.onEnableAdapters(getServer());

		getCommand("honeypot").setExecutor(new CommandManager());
		getHoneypotLogger().info("Loaded plugin");

		// When I save the file manually in VSCode it tends to format this section. If this looks weird, don't worry,
		// it'll still look fine
		getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "\n" + " _____                         _\n"
				+ "|  |  |___ ___ ___ _ _ ___ ___| |_\n" + "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED
				+ " TerrorByte\n" + ChatColor.GOLD + "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED
				+ this.getDescription().getVersion() + "\n" + ChatColor.GOLD + "                  |___|_|");

		if (isFolia()) {
			getHoneypotLogger().warning(
					"YOU ARE RUNNING ON FOLIA, AN EXPERIMENTAL SOFTWARE!!! It is assumed you know what you're doing, since this software can only be obtained via manually building it. Support for Folia is limited, be wary when using it for now!");
		}

		checkIfServerSupported();

		// Check for any updates
		new HoneypotUpdateChecker(this, "https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/version.txt")
				.getVersion(latest -> {

					if (Integer.parseInt(latest.replace(".", "")) > Integer
							.parseInt(this.getDescription().getVersion().replace(".", ""))) {
						getServer().getConsoleSender()
								.sendMessage(CommandFeedback.getChatPrefix() + ChatColor.RED
										+ " There is a new update available: " + latest
										+ ". Please download for the latest features and security updates!");
					}
					else {
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
		getHoneypotLogger().info("Stopping the ghost checker task");
		ghf.cancelTask();
		CacheManager.clearCache();
		getHoneypotLogger().info("Shut down plugin");
		getHoneypotLogger().info("Successfully shutdown Honeypot. Bye for now!");
	}

	/**
	 * Sets up the Permission hook for vault
	 * @return True if Vault is registered as a permission provider
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
		new HoneypotSupportedVersions(plugin, pluginVersion).getSupportedVersions(value -> {
			// Get the least supported and most supported server versions for this version
			// of Honeypot
			String[] lowerVersion = value.split("-")[0].split("\\.");
			String[] upperVersion = value.split("-")[1].split("\\.");

			int lowerMajorVer = Integer.parseInt(lowerVersion[0]);
			int lowerMinorVer = Integer.parseInt(lowerVersion[1]);
			int lowerRevisionVer = lowerVersion.length > 2 ? Integer.parseInt(lowerVersion[2]) : 0;

			int upperMajorVer = Integer.parseInt(upperVersion[1]);
			int upperMinorVer = Integer.parseInt(upperVersion[1]);
			int upperRevisionVer = lowerVersion.length > 2 ? Integer.parseInt(upperVersion[2]) : 0;

			// Check if the version the server is running is within the bounds of the
			// supported versions
			// This check is done because it allows the plugin to verify and
			// disable version check messages without updating the plugin code
			// This means if a minor MC version rolls out and doesn't affect functionality
			// to the plugin, we can update it on the GitHub side and server admins will not
			// see an error message
			if ((serverMajorVer < lowerMajorVer || serverMajorVer > upperMajorVer)
					&& (serverMinorVer < lowerMinorVer || serverMinorVer >= upperMinorVer)
					&& (serverRevisionVer < lowerRevisionVer || serverRevisionVer > upperRevisionVer)) {
				getHoneypotLogger().warning(
						"Honeypot is not guaranteed to support this version of Minecraft. We won't prevent you from using it, but some unusual behavior may occur, such as new blocks being processed strangely!");
				getHoneypotLogger().warning("Honeypot " + pluginVersion + " supports server versions " + value);
			}
		});

	}

	/**
	 * Check if the server is running on the experimental Folia software.
	 *
	 * @return True if Folia, false if anything else
	 */
	public static boolean isFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
		}
		catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	/*
	 * All the functions below are getter functions
	 *
	 * These simply return objects to prevent static keyword abuse
	 */

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
	 * Get the Behavior Registry
	 * @return {@link BehaviorRegistry}
	 */
	public static BehaviorRegistry getRegistry() {
		return registry;
	}

	/**
	 * Get the Ghost Honeypot Fixer
	 * @return {@link GhostHoneypotFixer}
	 */
	public static GhostHoneypotFixer getFixer() {
		return ghf;
	}
}