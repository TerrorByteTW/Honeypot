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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;

import java.util.List;

public class PistonExtendRetractListener implements Listener {

	/**
	 * Create private constructor to hide the implicit one
	 */
	PistonExtendRetractListener() {

	}

	// Player block break event
	@EventHandler(priority = EventPriority.LOW)
	public static void pistonPushEvent(BlockPistonExtendEvent event) {
		List<Block> blocks = event.getBlocks();
		for (Block b : blocks) {
			if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
				Honeypot.getHoneypotLogger().log(
						"PistonExtendEvent being called for Honeypot: " + b.getX() + ", " + b.getY() + "," + b.getZ());

				// Fire HoneypotNonPlayerBreakEvent
				HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getBlock(), event.getBlock());
				Bukkit.getPluginManager().callEvent(hnpbe);

				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public static void pistonPullEvent(BlockPistonRetractEvent event) {
		List<Block> blocks = event.getBlocks();
		for (Block b : blocks) {
			if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
				Honeypot.getHoneypotLogger().log("PistonRetractEvent being called for Honeypot: " + b.getX() + ", "
						+ b.getY() + ", " + b.getZ());

				// Fire HoneypotNonPlayerBreakEvent
				HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getBlock(), event.getBlock());
				Bukkit.getPluginManager().callEvent(hnpbe);

				event.setCancelled(true);
				break;
			}
		}
	}
}
