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
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPreTriggerEvent;
import org.reprogle.honeypot.api.events.HoneypotTriggerEvent;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.ArrayList;
import java.util.List;

public class EntityExplodeEventListener implements Listener, IHoneypotEvent {

    private final HoneypotLogger logger;
    private final BytePluginConfig config;
    private final HoneypotRegionManager regionManager;

    @Inject
    EntityExplodeEventListener(HoneypotLogger logger, BytePluginConfig config, HoneypotRegionManager regionManager) {
        this.logger = logger;
        this.config = config;
        this.regionManager = regionManager;
    }

    // Explosion listener
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent event) {
        // Get every block that would've been blown up
        List<Block> destroyedBlocks = event.blockList();
        ArrayList<Block> foundHoneypotBlocks = new ArrayList<>();
        boolean allowExplosions = config.config().getBoolean("allow-explode");

        // If a block of TNT was lit by a player, the explosion is treated as that player breaking the Honeypot
        Player igniter = event.getEntity() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player p ? p : null;

        // For every block, check if it was a Honeypot. If it was, check if explosions
        // are allowed.
        // If so, just delete the Honeypot. If not, cancel the explosion
        for (Block block : destroyedBlocks) {
            if (!regionManager.isHoneypotBlock(block)) continue;

            logger.debug(Component.text("EntityExplodeEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));

            if (igniter != null) {
                // BlockBreakEventListener handles adapters, trigger events, counting, history, and actions for the player.
                // If the break is disallowed or cancelled, it removes the Honeypot, which is checked for below
                logger.debug(Component.text("EntityExplodeEvent was caused by a player! Processing it as a break by " + igniter.getName()));
                Bukkit.getPluginManager().callEvent(new ExplosionBlockBreakEvent(block, igniter));
            } else {
                HoneypotPreTriggerEvent hpte = new HoneypotPreTriggerEvent(event.getEntity(), block, TriggerType.NON_PLAYER);
                Bukkit.getPluginManager().callEvent(hpte);

                // Same as a cancelled player break, the block is no longer treated as a Honeypot
                if (hpte.isCancelled()) {
                    logger.debug(Component.text("HoneypotPreTriggerEvent was cancelled, removing the Honeypot at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
                    regionManager.deleteRegionContaining(block);
                }
            }

            // If the Honeypot was removed above, the block gets blown up like any other
            if (!regionManager.isHoneypotBlock(block)) continue;

            // Fire HoneypotNonPlayerBreakEvent
            HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), block);
            Bukkit.getPluginManager().callEvent(hnpbe);

            // Player-lit explosions already fired a BREAK trigger through BlockBreakEventListener
            if (igniter == null) {
                HoneypotTriggerEvent hte = new HoneypotTriggerEvent(event.getEntity(), block, TriggerType.NON_PLAYER);
                Bukkit.getPluginManager().callEvent(hte);
            }

            if (allowExplosions) {
                regionManager.deleteRegionContaining(block);
            } else {
                foundHoneypotBlocks.add(block);
            }
        }

        destroyedBlocks.removeAll(foundHoneypotBlocks);

    }

}
