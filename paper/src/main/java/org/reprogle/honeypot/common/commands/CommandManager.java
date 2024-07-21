/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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

package org.reprogle.honeypot.common.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class CommandManager implements TabExecutor {

	// Inject all subcommands
	@Inject private Set<HoneypotSubCommand> subcommands;

	private final Honeypot plugin;
	private final HoneypotLogger logger;
	private final CommandFeedback commandFeedback;
	private final HoneypotConfigManager configManager;

	/**
	 * Registers all commands
	 */
	@Inject
	public CommandManager(Honeypot plugin, HoneypotLogger logger, HoneypotConfigManager configManager, CommandFeedback commandFeedback) {
		this.plugin = plugin;
		this.logger = logger;
		this.configManager = configManager;
		this.commandFeedback = commandFeedback;
	}

	/**
	 * Called by Bukkit when a player runs a command registered to our plugin. When
	 * called, the plugin will check if the
	 * sender is a player. If it is, it will first verify permissions, then verify
	 * if there were any subcommands. If
	 * not, show the GUI. If there were subcommands, but they aren't valid, show the
	 * usage.
	 * <p>
	 * If the sender is not a player, it will check if the command was reload. If it
	 * was, it'll allow the command to be
	 * run, otherwise it will throw an error.
	 *
	 * @param sender  The Sender sending the command. Not necessarily a player,
	 *                could be console or a plugin
	 * @param command The Command being executed
	 * @param label   The label of the command
	 * @param args    Any arguments passed to the command
	 * @return True if it ran successfully, false if it errored at any point.
	 *         Defaults as false
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {

		// Check if the command sender is a player
		if (sender instanceof Player p) {

			if (!(p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp())) {
				p.sendMessage(commandFeedback.sendCommandFeedback("no-permission"));
			}

			// If it's a player, ensure there is at least 1 argument given
			if (args.length > 0) {
				// For each subcommand in the subcommands array list, check if the argument is
				// the same as the command.
				// If so, run said subcommand
				for (HoneypotSubCommand subcommand : subcommands) {
					if (args[0].equalsIgnoreCase(subcommand.getName())) {
						try {
							if (!checkPermissions(p, subcommand)) {
								p.sendMessage(commandFeedback.sendCommandFeedback("no-permission"));
								return false;
							}

							subcommand.perform(p, args);
							return true;
						} catch (IOException e) {
							logger.severe(Component.text("Error while running command " + args[0] + "! Full stack trace: " + e));
						}
					}
				}

				p.sendMessage(commandFeedback.sendCommandFeedback("usage"));
			} else {
				// If no subcommands are passed, open the GUI. This is done by looping through
				// all the subcommands and
				// finding the GUI one, then performing it
				for (HoneypotSubCommand subcommand : subcommands) {
					if (subcommand.getName().equals("gui")) {
						try {
							if (!checkPermissions(p, subcommand)) {
								p.sendMessage(commandFeedback.sendCommandFeedback("no-permission"));
								return false;
							}

							subcommand.perform(p, args);
							return true;
						} catch (IOException e) {
							logger.severe(Component.text("Error while running command! Full stack trace: " + e));
						}
					}
				}
			}

		} else {
			if (args.length > 0 && args[0].equals("reload")) {
				try {
					configManager.getPluginConfig().reload();
					configManager.getPluginConfig().save();

					configManager.getGuiConfig().reload();
					configManager.getGuiConfig().save();

					configManager.getHoneypotsConfig().reload();
					configManager.getHoneypotsConfig().save();

					configManager.getLanguageFile().reload();
					configManager.getLanguageFile().save();

					plugin.getServer().getConsoleSender()
							.sendMessage(commandFeedback.sendCommandFeedback("reload"));
					return true;
				} catch (IOException e) {
					logger.severe(Component.text("Could not reload honeypot config! Full stack trace: " + e));
				}
			} else {
				ConsoleCommandSender console = plugin.getServer().getConsoleSender();
				console.sendMessage(commandFeedback.buildSplash(plugin));
				console.sendMessage(
						commandFeedback.getChatPrefix() + " Honeypot running on Spigot version " + Bukkit.getVersion());
				plugin.checkIfServerSupported();
			}
		}

		return false;
	}

	/**
	 * Returns a list of all subcommands for tab completion
	 *
	 * @return Set of all subcommands
	 */
	public Set<HoneypotSubCommand> getSubcommands() {
		return subcommands;
	}

	/**
	 * This function is responsible for tab completion of our pluign. It will check
	 * if the tab completer is at the first
	 * arg. If it is, return partial matches for the tab completer. If it's longer
	 * than one arg, return partial matches
	 * for the subcommand (such as create)
	 *
	 * @param sender  The sender of the command
	 * @param command The command being tab completed
	 * @param alias   The alias of the command
	 * @param args    The arguments passed to the tab completer (Required for tab
	 *                completion)
	 * @return A list of valid tab completed commands
	 */
	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		// Cast the CommandSender object to Player
		Player p = (Player) sender;

		// Only auto-complete if they have the permissions
		if (p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp()) {
			// If the argument is the first one return the subcommands
			if (args.length == 1) {
				// Create a subcommands array list and a subcommandsString array list to store
				// the subcommands as strings
				ArrayList<String> subcommandsTabComplete = new ArrayList<>();

				// Copy each partial match to the subcommands list
				StringUtil.copyPartialMatches(args[0], List.of(subcommands.stream().map(HoneypotSubCommand::getName).toArray(String[]::new)), subcommandsTabComplete);

				return subcommandsTabComplete;
			} else if (args.length >= 2) {
				// If the argument is the 2nd one or more, return the subcommands for that
				// subcommand
				for (HoneypotSubCommand subcommand : subcommands) {
					// Check if the first argument equals the command in the current interation
					if (args[0].equalsIgnoreCase(subcommand.getName())) {
						// Create a new array and copy partial matches of the current argument.
						// getSubcommands can actually handle more than one subcommand per
						// root command, meaning if the argument length is 3 or 4 or 5, it can handle
						// those accordingly. See HoneypotCreate.java for this in action
						ArrayList<String> subcommandsTabComplete = new ArrayList<>();

						StringUtil.copyPartialMatches(args[args.length - 1], subcommand.getSubcommands(p, args),
								subcommandsTabComplete);

						return subcommandsTabComplete;
					}
				}
			}
		}

		// If the argument does not exist at all
		return null;
	}

	/**
	 * Check if the Player has the permissions necessary to run the subcommand
	 *
	 * @param p          The player to check
	 * @param subcommand The subcommand we're checking
	 */
	private Boolean checkPermissions(Player p, HoneypotSubCommand subcommand) {
		boolean allowed = false;

		if (subcommand.getRequiredPermissions().isEmpty())
			return true;

		for (HoneypotPermission permission : subcommand.getRequiredPermissions()) {
			if (p.hasPermission(permission.permission())) {
				allowed = true;
				break;
			}
		}

		return allowed;
	}

}
