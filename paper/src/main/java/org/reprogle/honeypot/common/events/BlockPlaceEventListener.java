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
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class BlockPlaceEventListener implements Listener, IHoneypotEvent {
    private final HoneypotRegionManager regionManager;
    private final HoneypotLogger logger;

    @Inject
    public BlockPlaceEventListener(HoneypotRegionManager regionManager, HoneypotLogger logger) {
        this.regionManager = regionManager;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockPlaceEvent(BlockPlaceEvent event) {
        // Check to see if the event is canceled before doing any logic.
        if (event.isCancelled())
            return;

        // Early return if the block isn't a Honeypot
        if (!regionManager.isHoneypotBlock(event.getBlock())) return;

        logger.verbose(Component.text("BlockPlaceEvent (placement within region) being called for Honeypot: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ()));

        event.setCancelled(true);
    }

    // Believe it or not, placing water or lava does not trigger a BlockPlaceEvent
    @EventHandler(priority = EventPriority.LOWEST)
    public void bucketUseEvent(PlayerInteractEvent event) {

        // Check to see if the event is canceled before doing any logic.
        if (event.useInteractedBlock() == Event.Result.DENY)
            return;

        // Early return if not a right-click or no item in hand
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null)
            return;

        // Early return for null clicked block
        if (event.getClickedBlock() == null)
            return;

        // We only care about buckets
        if (event.getItem().getType() != Material.BUCKET && event.getItem().getType() != Material.WATER_BUCKET && event.getItem().getType() != Material.LAVA_BUCKET)
            return;

        if (!regionManager.isHoneypotBlock(event.getClickedBlock()) ||
            regionManager.isHoneypotBlock(event.getClickedBlock().getRelative(event.getBlockFace())))
            return;

        logger.verbose(Component.text("PlayerInteractEvent (fluid buckets within region) being called for Honeypot: " + event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ()));

        event.setCancelled(true);
    }
}
