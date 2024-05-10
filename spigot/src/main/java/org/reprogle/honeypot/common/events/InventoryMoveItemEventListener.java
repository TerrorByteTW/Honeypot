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

import com.google.inject.Inject;
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

	private HoneypotBlockManager blockManager;

	/**
	 * Create constructor to hide implicit one
	 */
	@Inject
	InventoryMoveItemEventListener(HoneypotBlockManager blockManager) {
		this.blockManager = blockManager;
	}

	// We're suppressing warnings on java:S2589 because location and world can both
	// equal null, but SonarLint thinks they can't
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	@SuppressWarnings("java:S2589")
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		InventoryType source = event.getSource().getType();
		if (!source.equals(InventoryType.HOPPER)
				&& !source.equals(InventoryType.DROPPER))
			return;

		Location location = event.getDestination().getLocation();
		World world = location.getWorld();

		if (location == null || world == null)
			return;

		Block targetBlock = world.getBlockAt(event.getDestination().getLocation());
		Block sourceBlock = world.getBlockAt(event.getSource().getLocation());

		boolean isSourceHoneypot = blockManager.isHoneypotBlock(sourceBlock);
		boolean isTargetHoneypot = blockManager.isHoneypotBlock(targetBlock);

		// We only care about hoppers and droppers, as these are the only two items that
		// can interact with chests directly
		if (isSourceHoneypot)
			return;

		// Check if the source was a Hopper or Dropper and if the destination is a
		// Honeypot. If so, cancel the whole
		// thing.
		if (isTargetHoneypot) {
			event.setCancelled(true);
		}
	}

}
