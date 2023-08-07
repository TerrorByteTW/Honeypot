/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2023
 * Copyright Honeypot Contributors (c) 2023
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

package org.reprogle.honeypot.common.providers.included;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.providers.Behavior;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.BehaviorType;

import javax.annotation.Nullable;

@Behavior(type = BehaviorType.NOTIFY, name = "notify", icon = Material.BEACON)
public class Notify extends BehaviorProvider {

	@Override
	public boolean process(Player p, @Nullable Block block) {

		if (block == null) return false;

		String chatPrefix = CommandFeedback.getChatPrefix();

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("honeypot.notify") || player.hasPermission("honeypot.*")
					|| player.isOp()) {
				player.sendMessage(chatPrefix + " " + ChatColor.RED + p.getName()
						+ " was caught breaking a Honeypot block at x=" + block.getX() + ", y="
						+ block.getY()
						+ ", z=" + block.getZ() + " in world " + block.getWorld().getName());
			}
		}

		Honeypot.plugin.getServer().getConsoleSender().sendMessage(chatPrefix + " " + ChatColor.RED
				+ p.getName() + " was caught breaking a Honeypot block");

		return true;
	}
}
