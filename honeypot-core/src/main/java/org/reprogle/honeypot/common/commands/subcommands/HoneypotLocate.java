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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.scheduler.BukkitRunnable;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HoneypotLocate implements HoneypotSubCommand {
	@Override
	public String getName() {
		return "locate";
	}

	@Override
	public void perform(Player p, String[] args) {
		final double radius = HoneypotConfigManager.getPluginConfig().getDouble("search-range");
		final double xCoord = p.getLocation().getX();
		final double yCoord = p.getLocation().getY();
		final double zCoord = p.getLocation().getZ();
		boolean potFound = false;

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
						potFound = true;

						// Create a dumb, invisible, invulnerable, block-sized glowing slime and spawn
						// it inside the
						// block
						Slime slime = (Slime) Objects.requireNonNull(Bukkit.getWorld(b.getWorld().getName()))
								.spawnEntity(b.getLocation().add(0.5, 0, 0.5), EntityType.SLIME);
						slime.setSize(2);
						slime.setAI(false);
						slime.setGlowing(true);
						slime.setInvulnerable(true);
						slime.setHealth(4.0);
						slime.setInvisible(true);

						// After 5 seconds, remove the slime. Setting its health to 0 causes the death
						// animation,
						// removing it just makes it go away. Poof!
						new BukkitRunnable() {

							@Override
							public void run() {
								slime.remove();
							}
						}.runTaskLater(Honeypot.plugin, 20L * 5);
					}
				}
			}
		}

		// Let the player know if a pot was found or not
		if (potFound) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("foundpot"));
		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("nopotfound"));
		}
	}

	// We don't have any subcommands here, but we cannot return null otherwise the
	// tab completer in the CommandManager
	// will throw an exception since CopyPartialMatches doesn't allow null values
	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.locate"));
		return permissions;
	}
}
