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

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;

public class StructureGrowEventListener implements Listener {

	/**
	 * Create package constructor to hide implicit one
	 */
	StructureGrowEventListener() {

	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public static void onStructureGrowEvent(StructureGrowEvent event) {
		for (int i = 0; i < event.getBlocks().size(); i++) {
			BlockState block = event.getBlocks().get(i);

			if (HoneypotBlockManager.getInstance().isHoneypotBlock(block.getBlock())) {
				Honeypot.getHoneypotLogger().debug("StuctureGrowEvent being cancelled for Honeypot located at "
						+ block.getX() + ", " + block.getY() + ", " + block.getZ());
				event.setCancelled(true);
			}
		}
	}

}
