/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

public class InventoryMoveItemEventListener implements Listener {

    private final HoneypotBlockManager blockManager;

    @Inject
    InventoryMoveItemEventListener(HoneypotBlockManager blockManager) {
        this.blockManager = blockManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        // We only care if the initiator is a dropper or hopper, meaning a dropper or hopper started the item move (Either a pull or a push)
        if (event.getInitiator().getType() != InventoryType.HOPPER && event.getInitiator().getType() != InventoryType.DROPPER)
            return;

        Location sourceLocation = event.getSource().getLocation();
        if (sourceLocation == null) return;

        Location destinationLocation = event.getDestination().getLocation();
        if (destinationLocation == null) return;

        World world = destinationLocation.getWorld();
        if (world == null) return;

        Block targetBlock = world.getBlockAt(destinationLocation);
        Block sourceBlock = world.getBlockAt(sourceLocation);

        boolean isSourceHoneypot = blockManager.isHoneypotBlock(sourceBlock);
        boolean isTargetHoneypot = blockManager.isHoneypotBlock(targetBlock);

        // Check if the source or target is a Honeypot. If so, cancel the whole thing.
        if (isSourceHoneypot || isTargetHoneypot) event.setCancelled(true);
    }
}