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
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPreTriggerEvent;
import org.reprogle.honeypot.api.events.HoneypotTriggerEvent;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class EntityChangeBlockEventListener implements Listener, IHoneypotEvent {

    private final HoneypotLogger logger;
    private final HoneypotRegionManager regionManager;
    private final BytePluginConfig config;
    
    @Inject
    EntityChangeBlockEventListener(HoneypotLogger logger, HoneypotRegionManager regionManager, BytePluginConfig config) {

        this.logger = logger;
        this.regionManager = regionManager;
        this.config = config;
    }

    // Enderman and silverfish event
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityChangeBlockEvent(EntityChangeBlockEvent event) {
        EntityType type = event.getEntity().getType();
        if (type != EntityType.ENDERMAN && type != EntityType.SILVERFISH) return;

        Block block = event.getBlock();
        if (!regionManager.isHoneypotBlock(block)) return;

        logger.verbose(Component.text("EntityChangeBlockEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));

        // If cancelled, the Honeypot is ignored. Remove it, since the block is about to be taken or infested
        if (!new HoneypotPreTriggerEvent(event.getEntity(), block, TriggerType.NON_PLAYER).callEvent()) {
            logger.debug(Component.text("HoneypotPreTriggerEvent was cancelled, removing the Honeypot at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            regionManager.deleteRegionContaining(block);
            return;
        }

        // Fire HoneypotNonPlayerBreakEvent
        new HoneypotNonPlayerBreakEvent(event.getEntity(), block).callEvent();
        new HoneypotTriggerEvent(event.getEntity(), block, TriggerType.NON_PLAYER).callEvent();

        // Endermen may take Honeypots if allowed, deleting the Honeypot. Silverfish are always blocked
        if (type == EntityType.ENDERMAN && config.config().getBoolean("allow-enderman")) {
            regionManager.deleteRegionContaining(block);
        } else {
            event.setCancelled(true);
        }
    }
}
