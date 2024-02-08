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

package org.reprogle.honeypot.common.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;

public class InventoryMoveItemEventListener implements Listener {

	/**
	 * Create constructor to hide implicit one
	 */
	InventoryMoveItemEventListener() {

	}

	@EventHandler(priority = EventPriority.LOW)
	public static void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		// Get the inventory type of the source block
		InventoryType source = event.getSource().getType();

		// Get the location of the destination block, and the world. These require null checks
		Location location = event.getDestination().getLocation();
		if (location == null)
			return;

		World world = location.getWorld();
		if (world == null)
			return;

		Block block = world.getBlockAt(event.getDestination().getLocation());

		// Check if the source was a Hopper or Dropper and if the destination is a Honeypot. If so, cancel the whole
		// thing.
		if ((source.equals(InventoryType.HOPPER) || source.equals(InventoryType.DROPPER))
				&& HoneypotBlockManager.getInstance().isHoneypotBlock(block)) {
			event.setCancelled(true);
		}
	}

}
