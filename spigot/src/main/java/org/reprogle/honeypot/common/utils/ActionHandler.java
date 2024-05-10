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

package org.reprogle.honeypot.common.utils;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;

import java.util.List;

// There are a ton of deprecation warnings in this class due to ChatColor. I'm using ChatColor because I support Spigot as well, not just Paper.
// However, the deprecation warnings are here because I also have Folia as a dependency, and that dependency is "above" Spigot in the dependency list. So, it's taking precendence
@SuppressWarnings("deprecation")
public class ActionHandler {

	private final Honeypot plugin;
	private final HoneypotLogger logger;
	private final HoneypotConfigManager configManager;
	private final CommandFeedback commandFeedback;

	@Inject
	public ActionHandler(Honeypot plugin, HoneypotLogger logger, HoneypotConfigManager configManager, CommandFeedback commandFeedback) {
		this.plugin = plugin;
		this.logger = logger;
		this.configManager = configManager;
		this.commandFeedback = commandFeedback;
	}

	@SuppressWarnings({ "java:S3776", "java:S2629", "java:S1192", "java:S6541" })
	public void handleCustomAction(String action, Block block, Player player) {

		plugin.getHoneypotLogger().debug("Handling action " + action + " for player " + player.getName()
				+ " at location " + block.getLocation());

		// Behavior providers take higher precedence over custom config actions.
		if (Honeypot.getRegistry().getBehaviorProvider(action) != null) {
			Honeypot.processor.process(Honeypot.getRegistry().getBehaviorProvider(action), player, block);
			return;
		}

		// Default path is likely due to custom actions. Run whatever the action was
		YamlDocument config = configManager.getHoneypotsConfig();
		if (config.contains(action)) {
			List<String> commands = config.getStringList(action + ".commands");
			List<String> permissionsAdd = config.getStringList(action + ".permissions-add");
			List<String> permissionsRemove = config.getStringList(action + ".permissions-remove");
			List<String> broadcasts = config.getStringList(action + ".broadcasts");
			List<String> messages = config.getStringList(action + ".messages");

			if (!commands.isEmpty()) {
				for (String command : commands) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							formatCommand(command, block, player));
				}
			}

            if (!messages.isEmpty()) {
				for (String message : messages) {
					player.sendMessage(formatMessage(message, block, player));
				}
			}

			if (!broadcasts.isEmpty()) {
				for (String broadcast : broadcasts) {
					plugin.getServer().broadcastMessage(formatMessage(broadcast, block, player));
				}
			}

			if (Honeypot.getPermissions() != null) {
				if (!permissionsAdd.isEmpty()) {
					for (String permission : permissionsAdd) {
						Honeypot.getPermissions().playerAdd(null, player, permission);
					}
				}

				if (!permissionsRemove.isEmpty()) {
					for (String permission : permissionsRemove) {
						Honeypot.getPermissions().playerRemove(null, player, permission);
					}
				}
			}
			// I'd like to warn them if the tried to adjust permissions without vault. If vault is null and they
			// *didn't* try to adjust permissions, then who cares?
			else if (!permissionsAdd.isEmpty() || !permissionsRemove.isEmpty()) {
				logger.warning(commandFeedback.getChatPrefix() + ChatColor.RED
						+ " Vault is not installed, Honeypots that modify permissions won't work. Please download here: https://www.spigotmc.org/resources/vault.34315/");
			}
		}
		else {
			logger.warning("A Honeypot tried to run using action: " + action
					+ ", but that action doesn't exist! Please verify your honeypots.yml config");
		}
	}

	private static String formatMessage(String message, Block block, Player player) {
		String formattedString = message.replace("%player%", player.getName());
		formattedString = formattedString.replace("%pLocation%",
				player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ());
		formattedString = formattedString.replace("%bLocation%",
				block.getLocation().getX() + " " + block.getLocation().getY() + " " + block.getLocation().getZ());
		formattedString = formattedString.replace("%world%", block.getLocation().getWorld().getName());

		// Support Placeholder API!!!! This will parse any remaining placeholders in the message
		formattedString = PlaceholderAPI.setPlaceholders(player, formattedString);

		return ChatColor.translateAlternateColorCodes('&', formattedString);
	}

	private static String formatCommand(String command, Block block, Player player) {
		String formattedCommand = command.replace("%player%", player.getName());
		formattedCommand = formattedCommand.replace("%pLocation%",
				player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ());
		formattedCommand = formattedCommand.replace("%bLocation%",
				block.getLocation().getX() + " " + block.getLocation().getY() + " " + block.getLocation().getZ());
		formattedCommand = formattedCommand.replace("%world%", block.getLocation().getWorld().getName());

		// Support Placeholder API!!!! This will parse any remaining placeholders in the command
		formattedCommand = PlaceholderAPI.setPlaceholders(player, formattedCommand);

		return formattedCommand;
	}
}
