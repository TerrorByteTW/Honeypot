/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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
import org.bukkit.event.block.BlockFromToEvent;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class BlockFromToEventListener implements Listener, IHoneypotEvent {
    private final HoneypotRegionManager regionManager;
    private final HoneypotLogger logger;

    @Inject
    BlockFromToEventListener(HoneypotRegionManager regionManager, HoneypotLogger logger) {
        this.regionManager = regionManager;
        this.logger = logger;
    }

    /**
     * Block water from flowing into Honeypot blocks (Such as torches)
     *
     * @param event The BlockFromToEvent, passed from Bukkit's event handler
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void blockFromToEvent(BlockFromToEvent event) {
        if (event.getFace() == BlockFace.DOWN)
            return;

        Block toBlock = event.getToBlock();
        if (regionManager.isHoneypotBlock(toBlock)) {
            logger.verbose(Component.text("BlockFromToEvent being called for Honeypot: " + event.getToBlock().getX() + ", " + event.getToBlock().getY() + ", " + event.getToBlock().getZ()));
            if (event.getFace() != BlockFace.DOWN)
                event.setCancelled(true);
        }
    }

}
