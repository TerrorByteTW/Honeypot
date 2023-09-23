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

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;

import java.util.List;

public class ActionHandler {

	private ActionHandler() {
	}

	@SuppressWarnings({"java:S3776", "java:S2629", "java:S1192"})
	public static void handleCustomAction(String action, Block block, Player player) {

		Honeypot.getHoneypotLogger().log("Handling action " + action + " for player " + player.getName() + " at location " + block.getLocation());

		// Behavior providers take higher precedence over custom config actions.
		if (Honeypot.getRegistry().getBehaviorProvider(action) != null) {
			Honeypot.processor.process(Honeypot.getRegistry().getBehaviorProvider(action), player, block);
			return;
		}

		// Default path is likely due to custom actions. Run whatever the action was
		YamlDocument config = HoneypotConfigManager.getHoneypotsConfig();
		if (config.contains(action)) {
			String type = config.getString(action + ".type");
			switch (type) {
				case "command" -> {
					List<String> commands = config.getStringList(action + ".commands");
					List<String> messages = config.getStringList(action + ".messages");
					if (commands.isEmpty()) {
						Honeypot.plugin.getLogger().warning(
								"Commands list is empty for Honeypot type " + action
										+ "! Please verify config");
						return;
					}

					for (String command : commands) {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
								formatCommand(command, block, player));
					}

					if (!messages.isEmpty()) {
						for (String message : messages) {
							player.sendMessage(formatMessage(message, block, player));
						}
					}
				}

				case "permission" -> {
					if (Honeypot.getPermissions() == null) {
						Honeypot.plugin.getLogger().warning(
								CommandFeedback.getChatPrefix() + ChatColor.RED + " Vault is not installed, permission Honeypots won't work");
						Honeypot.getHoneypotLogger().log(
								"Vault is not installed. Permission Honeypots won't work. Please download here: https://www.spigotmc.org/resources/vault.34315/");
						return;
					}
					
					List<String> permissionsAdd = config.getStringList(action + ".permissions-add");
					List<String> permissionsRemove = config.getStringList(action + ".permissions-remove");
					List<String> messages = config.getStringList(action + ".messages");
					if (permissionsAdd.isEmpty() && permissionsRemove.isEmpty()) {
						Honeypot.plugin.getLogger()
								.warning("Permissions lists are empty for Honeypot type "
										+ action + "! Please verify config");
						return;
					}

					for (String permission : permissionsAdd) {
						Honeypot.getPermissions().playerAdd(null, player, permission);
					}

					for (String permission : permissionsRemove) {
						Honeypot.getPermissions().playerRemove(null, player, permission);
					}

					if (!messages.isEmpty()) {
						for (String message : messages) {
							player.sendMessage(formatMessage(message, block, player));
						}
					}

				}

				case "broadcast" -> {
					List<String> broadcasts = config.getStringList(action + ".broadcasts");
					List<String> messages = config.getStringList(action + ".messages");

					if (broadcasts.isEmpty()) {
						Honeypot.plugin.getLogger().warning(
								"Broadcasts list is empty for Honeypot type " + action
										+ "! Please verify config");
						return;
					}

					for (String broadcast : broadcasts) {
						Honeypot.plugin.getServer().broadcastMessage(formatMessage(broadcast, block, player));
					}

					if (!messages.isEmpty()) {
						for (String message : messages) {
							player.sendMessage(formatMessage(message, block, player));
						}
					}
				}

				default -> {
					Honeypot.plugin.getLogger().warning("Honeypot " + action
							+ " tried to run as a type that doesn't exist! Please verify config");
				}
			}
		}
	}

	private static String formatMessage(String message, Block block, Player player) {
		String formattedString = message.replace("%player%", player.getName());
		formattedString = formattedString.replace("%pLocation%", player.getLocation().getX() + " "
				+ player.getLocation().getY() + " " + player.getLocation().getZ());
		formattedString = formattedString.replace("%bLocation%", block.getLocation().getX() + " "
				+ block.getLocation().getY() + " " + block.getLocation().getZ());
		formattedString = formattedString.replace("%world%", block.getLocation().getWorld().getName());

		return ChatColor.translateAlternateColorCodes('&', formattedString);
	}

	private static String formatCommand(String command, Block block, Player player) {
		String formattedCommand = command.replace("%player%", player.getName());
		formattedCommand = formattedCommand.replace("%pLocation%", player.getLocation().getX() + " "
				+ player.getLocation().getY() + " " + player.getLocation().getZ());
		formattedCommand = formattedCommand.replace("%bLocation%", block.getLocation().getX() + " "
				+ block.getLocation().getY() + " " + block.getLocation().getZ());
		formattedCommand = formattedCommand.replace("%world%", block.getLocation().getWorld().getName());

		return formattedCommand;
	}
}
