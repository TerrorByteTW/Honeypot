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

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;

public class BlockFromToEventListener implements Listener {

	/**
	 * Create package listener to hide implicit one
	 */
	BlockFromToEventListener() {

	}

	/**
	 * Block water from flowing into Honeypot blocks (Such as torches)
	 *
	 * @param event The BlockFromToEvent, passed from Bukkit's event handler
	 */
	@EventHandler(priority = EventPriority.LOW)
	public static void blockFromToEvent(BlockFromToEvent event) {
		Block toBlock = event.getToBlock();
		if (HoneypotBlockManager.getInstance().isHoneypotBlock(toBlock)) {
			Honeypot.getHoneypotLogger().debug("BlockFromToEvent being called for Honeypot: " + toBlock.getX() + ", "
					+ toBlock.getY() + ", " + toBlock.getZ());
			event.setCancelled(true);
		}
	}

}
