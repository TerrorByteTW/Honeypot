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

package org.reprogle.honeypot.commands.subcommands;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.utils.HoneypotConfigManager;
import org.reprogle.honeypot.utils.HoneypotPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("java:S1192")
public class HoneypotUpgrade implements HoneypotSubCommand {

	@Override
	public String getName() {
		return "upgrade";
	}

	@Override
	public void perform(Player p, String[] args) throws IOException {
		if (HoneypotConfigManager.getHoneypotsConfig().contains("upgraded")
				&& Boolean.TRUE.equals(HoneypotConfigManager.getHoneypotsConfig().getBoolean("upgraded"))) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("alreadyupgraded"));
			return;
		}

		if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
			List<HoneypotBlockObject> oldBlocks = HoneypotBlockManager.getInstance().getAllHoneypots();
			int customBlock = 0;

			for (HoneypotBlockObject block : oldBlocks) {
				String action = block.getAction();
				if (!"Fban".equals(action) && !"warn".equals(action) && !"kick".equals(action)
						&& !"notify".equals(action) && !"nothing".equals(action)) {
					List<String> commands = new ArrayList<>();
					commands.add(action);

					String route = "import" + customBlock;

					HoneypotConfigManager.getHoneypotsConfig().createSection(route);

					HoneypotConfigManager.getHoneypotsConfig().createSection(route + ".type");
					HoneypotConfigManager.getHoneypotsConfig().set(route + ".type", "command");

					HoneypotConfigManager.getHoneypotsConfig().createSection(route + ".commands");
					HoneypotConfigManager.getHoneypotsConfig().set(route + ".commands", commands);

					++customBlock;

					HoneypotBlockManager.getInstance().deleteBlock(block.getBlock());
					HoneypotBlockManager.getInstance().createBlock(block.getBlock(), route);
				}
			}

			// Mark custom actions as having already been upgraded to prevent an accidental
			// double-upgrade (Which will break things)
			HoneypotConfigManager.getHoneypotsConfig().createSection("upgraded").set("upgraded", true);

			HoneypotConfigManager.getHoneypotsConfig().save();
			p.sendMessage(CommandFeedback.sendCommandFeedback("success"));

		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("upgrade"));
		}
	}

	// Even though this command has subcommands, I do not want to return any
	// to avoid the player confirming an upgrade before they read the warning
	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.upgrade"));
		return permissions;
	}

}
