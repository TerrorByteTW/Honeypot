/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPreTriggerEvent;
import org.reprogle.honeypot.api.events.HoneypotTriggerEvent;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class PistonExtendRetractListener implements Listener, IHoneypotEvent {

	private final HoneypotRegionManager regionManager;
	private final HoneypotLogger logger;

	@Inject
	PistonExtendRetractListener(HoneypotRegionManager regionManager, HoneypotLogger logger) {
		this.regionManager = regionManager;
		this.logger = logger;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void pistonPushEvent(BlockPistonExtendEvent event) {
		BlockFace direction = event.getDirection();

		// A Honeypot is affected if it's pushed, or sits where a pushed block or the piston head is moving into (e.g. it gets broken)
		Set<Block> affected = new LinkedHashSet<>();
		affected.add(event.getBlock().getRelative(direction));
		for (Block b : event.getBlocks()) {
			affected.add(b);
			affected.add(b.getRelative(direction));
		}

		if (triggerHoneypots(event.getBlock(), affected, "PistonExtendEvent")) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void pistonPullEvent(BlockPistonRetractEvent event) {
		if (triggerHoneypots(event.getBlock(), new LinkedHashSet<>(event.getBlocks()), "PistonRetractEvent")) {
			event.setCancelled(true);
		}
	}

	/**
	 * Fires the trigger events for every Honeypot region touched by a piston, once per region
	 *
	 * @param piston   The piston block
	 * @param affected The blocks affected by the piston
	 * @param source   The name of the Bukkit event, for logging
	 * @return True if the piston should be cancelled because at least one Honeypot was triggered
	 */
	private boolean triggerHoneypots(Block piston, Set<Block> affected, String source) {
		Set<HoneypotRegionObject> triggeredRegions = new HashSet<>();
		boolean cancel = false;

		for (Block honeypot : affected) {
			if (!regionManager.isHoneypotBlock(honeypot)) continue;

			HoneypotRegionObject region = regionManager.getHoneypotRegion(honeypot);
			if (region != null && !triggeredRegions.add(region)) {
				// Already triggered by this piston, but it still needs protecting
				cancel = true;
				continue;
			}

			logger.verbose(Component.text(source + " being called for Honeypot: " + honeypot.getX() + ", " + honeypot.getY() + ", " + honeypot.getZ()));

			if (!new HoneypotPreTriggerEvent(honeypot, TriggerType.NON_PLAYER).callEvent()) {
				// The Honeypot is about to be moved or broken, so remove it rather than leaving a ghost region behind
				logger.debug(Component.text("HoneypotPreTriggerEvent was cancelled, removing the Honeypot at " + honeypot.getX() + ", " + honeypot.getY() + ", " + honeypot.getZ()));
				regionManager.deleteRegionContaining(honeypot);
				continue;
			}

			new HoneypotNonPlayerBreakEvent(piston, honeypot).callEvent();
			new HoneypotTriggerEvent(honeypot, TriggerType.NON_PLAYER).callEvent();
			cancel = true;
		}

		return cancel;
	}
}
