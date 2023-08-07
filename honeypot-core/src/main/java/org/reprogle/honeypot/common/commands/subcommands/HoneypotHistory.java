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

package org.reprogle.honeypot.common.commands.subcommands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"java:S1192", "java:S3776"})
public class HoneypotHistory implements HoneypotSubCommand {

	@Override
	public String getName() {
		return "history";
	}

	@Override
	public void perform(Player p, String[] args) {
		if (args.length >= 3 && (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("query"))) {
			Player argPlayer = Bukkit.getPlayer(args[2]);

			if (argPlayer == null || !Bukkit.getPlayer(args[2]).isOnline()) {
				p.sendMessage(CommandFeedback.sendCommandFeedback("notonline"));
				return;
			}

			if (args[1].equalsIgnoreCase("query")) {
				p.sendMessage(CommandFeedback.sendCommandFeedback("searching"));

				List<HoneypotPlayerHistoryObject> history = HoneypotPlayerHistoryManager.getInstance()
						.getPlayerHistory(argPlayer);
				int length = HoneypotConfigManager.getPluginConfig().getInt("history-length");

				if (history.size() > length) {
					p.sendMessage(CommandFeedback.sendCommandFeedback("truncating"));
				}

				if (history.isEmpty()) {
					p.sendMessage(CommandFeedback.sendCommandFeedback("nohistory"));
					return;
				}

				int limit = Math.min(history.size(), length);

				for (int i = 0; i < limit; i++) {
					p.sendMessage(ChatColor.GOLD + "\n-------[ " + ChatColor.WHITE + history.get(i).getDateTime()
							+ ChatColor.GOLD + " ]-------");
					TextComponent playerInfo = new TextComponent(
							"Player: " + ChatColor.GOLD + history.get(i).getPlayer() + ChatColor.WHITE + " @ "
									+ ChatColor.WHITE + ChatColor.GOLD + history.get(i).getHoneypot().getWorld() + " "
									+ history.get(i).getHoneypot().getCoordinates());
					playerInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							"/hpteleport " + (history.get(i).getHoneypot().getLocation().getX() + 0.5) + " "
									+ (history.get(i).getHoneypot().getLocation().getY() + 1) + " "
									+ (history.get(i).getHoneypot().getLocation().getZ() + 0.5)));
					playerInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new Text(ChatColor.GRAY + "" + ChatColor.ITALIC + "Click to teleport")));

					p.spigot().sendMessage(playerInfo);
					p.sendMessage("Action: " + ChatColor.GOLD + history.get(i).getHoneypot().getAction());
					p.sendMessage(ChatColor.GOLD + "----------------------------------");
				}

			} else if (args[1].equalsIgnoreCase("delete")) {
				if (args.length >= 4) {
					HoneypotPlayerHistoryManager.getInstance().deletePlayerHistory(argPlayer,
							Integer.parseInt(args[3]));
				} else {
					HoneypotPlayerHistoryManager.getInstance().deletePlayerHistory(argPlayer);
				}
				p.sendMessage(CommandFeedback.sendCommandFeedback("success"));
			}
		} else if (args.length == 2 && args[1].equalsIgnoreCase("purge")) {
			HoneypotPlayerHistoryManager.getInstance().deleteAllHistory();
			p.sendMessage(CommandFeedback.sendCommandFeedback("success"));
		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
		}

	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		List<String> subcommands = new ArrayList<>();

		// Base arguments
		if (args.length == 2) {
			subcommands.add("delete");
			subcommands.add("query");
			subcommands.add("purge");
			// If the args length is 3 and they passed a valid sub-subcommand (yikes), do
			// this
		} else if (args.length == 3 && (args[1].equalsIgnoreCase("query") || args[1].equalsIgnoreCase("delete"))) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				subcommands.add(player.getName());
			}
			// If the args length is 4 and they typed delete, just give them a list of
			// numbers
		} else if (args.length == 4 && args[1].equalsIgnoreCase("delete")) {
			for (int i = 1; i < 10; i++) {
				subcommands.add(Integer.toString(i));
			}
		}

		return subcommands;
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.history"));
		return permissions;
	}

}
