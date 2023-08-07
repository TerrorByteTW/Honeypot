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

package org.reprogle.honeypot.common.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;

public class PlayerCommandPreprocessEventListener implements Listener {

	/**
	 * Create private constructor to hide the implicit one
	 */
	PlayerCommandPreprocessEventListener() {

	}

	@EventHandler(priority = EventPriority.LOW)
	public static void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().startsWith("/hpteleport")) {
			event.setCancelled(true);
			if (event.getPlayer().hasPermission("honeypot.teleport")) {
				String rawCommand = event.getMessage();
				String processedCommand = rawCommand.replace("/hpteleport",
						"minecraft:tp " + event.getPlayer().getName());

				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), processedCommand);
			} else {
				event.getPlayer().sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
			}

		}
	}

}
