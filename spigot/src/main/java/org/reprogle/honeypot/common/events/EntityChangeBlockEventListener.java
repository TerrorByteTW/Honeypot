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
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

public class EntityChangeBlockEventListener implements Listener {

	/**
	 * Create package constructor to hide implicit one
	 */
	EntityChangeBlockEventListener() {

	}

	// Enderman event
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public static void entityChangeBlockEvent(EntityChangeBlockEvent event) {

		// If the entity grabbing the block is an enderman, if they are allowed to,
		// delete the
		// Honeypot, otherwise cancel it
		if (event.getEntity().getType().equals(EntityType.ENDERMAN)) {
			if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(event.getBlock()))) {

				Honeypot.getHoneypotLogger().debug("EntityChangeBlockEvent being called for Honeypot: "
						+ event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());

				// Fire HoneypotNonPlayerBreakEvent
				HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(),
						event.getBlock());
				Bukkit.getPluginManager().callEvent(hnpbe);

				if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-enderman"))) {
					HoneypotBlockManager.getInstance().deleteBlock(event.getBlock());
				} else {
					event.setCancelled(true);
				}
			}
		} else if (event.getEntity().getType().equals(EntityType.SILVERFISH)
				&& Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(event.getBlock()))) {

			// Fire HoneypotNonPlayerBreakEvent
			HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), event.getBlock());
			Bukkit.getPluginManager().callEvent(hnpbe);

			event.setCancelled(true);
		}
	}
}
