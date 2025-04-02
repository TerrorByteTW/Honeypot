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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class BlockFormEventListener implements Listener {

	private final HoneypotLogger logger;
	private final HoneypotBlockManager blockManager;

	/**
	 * Create package listener to hide implicit one
	 */
	@Inject
	BlockFormEventListener(HoneypotLogger logger, HoneypotBlockManager blockManager) {
		this.logger = logger;
		this.blockManager = blockManager;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockFormEvent(BlockFormEvent event) {
		Block block = event.getBlock();

		if (blockManager.isHoneypotBlock(block)) {
			logger.debug(Component.text("BlockFormEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
			event.setCancelled(true);
		}
	}

}
