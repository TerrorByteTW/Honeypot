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
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.util.ArrayList;
import java.util.List;

public class EntityExplodeEventListener implements Listener {

	/**
	 * Create package constructor to hide implicit one
	 */
	EntityExplodeEventListener() {

	}

	// Explosion listener
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public static void entityExplodeEvent(EntityExplodeEvent event) {
		// Get every block that would've been blown up
		List<Block> destroyedBlocks = event.blockList();
		ArrayList<Block> foundHoneypotBlocks = new ArrayList<>();
		boolean allowExplosions = HoneypotConfigManager.getPluginConfig().getBoolean("allow-explode");
		Entity e = event.getEntity();
		Entity source = null;

		if (e instanceof TNTPrimed tnt) {
			source = tnt.getSource();
		}

		// For every block, check if it was a Honeypot. If it was, check if explosions
		// are allowed.
		// If so, just delete the Honeypot. If not, cancel the explosion
		for (Block block : destroyedBlocks) {
			if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(block))) {
				Honeypot.getHoneypotLogger()
						.debug("EntityExplodeEvent being called for Honeypot: " + block.getX() + ", "
								+ block.getY() + ", " + block.getZ());

				if (source instanceof Player) {
					HoneypotPlayerHistoryManager.getInstance().addPlayerHistory((Player) source,
							HoneypotBlockManager.getInstance().getHoneypotBlock(block));
					Honeypot.getHoneypotLogger().debug(
							"EntityExplodeEvent was caused by a player! It has been logged in the history, and the Honeypot's action has been triggered for that player. Player was: "
									+ source.getName());

					// Call a BlockBreakEvent for that player, as they attempted to break the block
					// in the first place.
					BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, (Player) source);
					Bukkit.getPluginManager().callEvent(blockBreakEvent);
				}

				// Fire HoneypotNonPlayerBreakEvent
				HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), block);
				Bukkit.getPluginManager().callEvent(hnpbe);

				if (allowExplosions) {
					HoneypotBlockManager.getInstance().deleteBlock(block);
				} else {
					foundHoneypotBlocks.add(block);
				}
			}
		}

		destroyedBlocks.removeAll(foundHoneypotBlocks);

	}

}
