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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.List;

public class HoneypotRemove implements HoneypotSubCommand {
	@Override
	public String getName() {
		return "remove";
	}

	@Override
	@SuppressWarnings("java:S3776")
	public void perform(Player p, String[] args) {
		Block block = p.getTargetBlockExact(5);

		if (args.length >= 2) {
			switch (args[1].toLowerCase()) {
				case "all" -> {
					HoneypotBlockManager.getInstance().deleteAllHoneypotBlocks(p.getWorld());
					p.sendMessage(CommandFeedback.sendCommandFeedback("deletedall"));
				}

				case "near" -> {
					final double radius = HoneypotConfigManager.getPluginConfig().getDouble("search-range");
					final double xCoord = p.getLocation().getX();
					final double yCoord = p.getLocation().getY();
					final double zCoord = p.getLocation().getZ();

					// For every x value within radius
					for (double x = xCoord - radius; x < xCoord + radius; x++) {
						// For every y value within radius
						for (double y = yCoord - radius; y < yCoord + radius; y++) {
							// For every z value within radius
							for (double z = zCoord - radius; z < zCoord + radius; z++) {

								// Check the block at coords x,y,z to see if it's a Honeypot
								final Block b = new Location(p.getWorld(), x, y, z).getBlock();

								// If it is a honeypot do this
								if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
									HoneypotBlockManager.getInstance().deleteBlock(b);

								}
							}
						}
					}

					p.sendMessage(CommandFeedback.sendCommandFeedback("deletednear"));
				}

				default -> potRemovalCheck(block, p);
			}
		} else {
			potRemovalCheck(block, p);
		}
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		List<String> subcommands = new ArrayList<>();

		if (args.length == 2) {
			// Return all action types for the /honeypot create command
			subcommands.add("all");
			subcommands.add("near");
		}

		return subcommands;
	}

	private void potRemovalCheck(Block block, Player p) {
		assert block != null;
		if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(block))) {
			HoneypotBlockManager.getInstance().deleteBlock(block);
			p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));
		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
		}
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.remove"));
		permissions.add(new HoneypotPermission("honeypot.break"));
		return permissions;
	}
}
