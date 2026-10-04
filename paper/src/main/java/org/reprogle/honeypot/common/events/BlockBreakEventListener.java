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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.*;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

public class BlockBreakEventListener implements Listener, IHoneypotEvent {

    private static final String BREAK_PERMISSION = "honeypot.break";

    private static final String WILDCARD_PERMISSION = "honeypot.*";

    private final ActionHandler actionHandler;
    private final HoneypotLogger logger;
    private final HoneypotRegionManager regionManager;
    private final BytePluginConfig config;
    private final CommandFeedback commandFeedback;
    private final AdapterManager adapterManager;

    @Inject
    public BlockBreakEventListener(ActionHandler actionHandler, HoneypotLogger logger, HoneypotRegionManager regionManager,
                                   BytePluginConfig config, CommandFeedback commandFeedback, AdapterManager adapterManager) {
        this.actionHandler = actionHandler;
        this.logger = logger;
        this.regionManager = regionManager;
        this.config = config;
        this.commandFeedback = commandFeedback;
        this.adapterManager = adapterManager;
    }

    // Player block break event
    @EventHandler(priority = EventPriority.LOWEST)
    public void blockBreakEvent(BlockBreakEvent event) {
        // Check to see if the event is canceled before doing any logic.
        // Ex: Creative mode player with Sword in hand
        if (event.isCancelled())
            return;

        // Get the player associated with the event
        Player player = event.getPlayer();

        // Early return if the block isn't a Honeypot
        if (!regionManager.isHoneypotBlock(event.getBlock())) return;

        // If any of the adapters state that this is a disallowed action, don't bother doing anything since it was already blocked
        // It really shouldn't be in this region anyway, so it's going to be removed
        if (!adapterManager.checkAllAdapters(player, event.getBlock().getLocation())) {
            regionManager.deleteRegionContaining(event.getBlock());
            return;
        }

        // Fire HoneypotPrePlayerBreakEvent
        var hppbe = new HoneypotPrePlayerBreakEvent(player, event.getBlock());
        var hpte = new HoneypotPreTriggerEvent(player, event.getBlock(), TriggerType.BREAK);
        logger.debug(Component.text("HoneypotPrePlayerBreakEvent is being called for " + player));

        // Check if the event was canceled. If it is, delete the block.
        if (!hppbe.callEvent()) {
            regionManager.deleteRegionContaining(event.getBlock());
            logger.debug(Component.text("DEPRECATED HoneypotPrePlayerBreakEvent for " + player + " was cancelled, not continuing."));
            return;
        }

        // Check if the event was canceled. If it is, delete the block.
        if (!hpte.callEvent()) {
            regionManager.deleteRegionContaining(event.getBlock());
            logger.debug(Component.text("HoneypotPreTriggerEvent for " + player + " was cancelled, not continuing."));
            return;
        }

        // Create a boolean for if we should remove the block from the DB or not
        boolean deleteBlock = false;

        // If Allow Player Destruction is true, the player has permissions, or is Op,
        // flag the block for deletion from the DB
        // Otherwise, set the BlockBreakEvent to canceled
        if (config.config().getBoolean("allow-player-destruction")
            || player.hasPermission(BREAK_PERMISSION)
            || player.hasPermission(WILDCARD_PERMISSION) || player.isOp()) {
            deleteBlock = true;
            logger.debug(Component.text("Player " + player + " is either allowed to break Honeypots or has some sort of permission. This Honeypot will be removed from the world"));
        } else {
            event.setCancelled(true);
        }

        // Count the break, log it, and run the action if the player has hit the trigger limit
        if (actionHandler.checkAndHandle(player, event.getBlock(), TriggerType.BREAK) == ActionHandler.TriggerResult.EXEMPT
            && (player.hasPermission(BREAK_PERMISSION) || player.hasPermission(WILDCARD_PERMISSION) || player.isOp())) {
            player.sendMessage(commandFeedback.sendCommandFeedback("staff-broke"));
        }

        // Fire HoneypotPlayerBreakEvent
        new HoneypotPlayerBreakEvent(player, event.getBlock()).callEvent();
        new HoneypotTriggerEvent(player, event.getBlock(), TriggerType.BREAK).callEvent();
        logger.debug(Component.text("HoneypotPlayerBreakEvent is being called for " + player));

        // If we flagged the block for deletion, remove it from the DB. Do this after
        // other actions have been
        // completed, otherwise the other actions will fail with NPEs
        if (deleteBlock) {
            logger.debug(Component.text("Block is flagged for deletion, removing it from storage"));
            regionManager.deleteRegionContaining(event.getBlock());
        }
    }

    // This is a separate event from the one above. We want to know if any Honeypots
    // were broken due to breaking a
    // supporting block, such as torches breaking due to
    // the block they're on being broken
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void checkBlockBreakSideEffects(BlockBreakEvent event) {
        if (!config.config().getBoolean("allow-player-destruction"))
            return;

        Player player = event.getPlayer();

        if (!player.hasPermission(BREAK_PERMISSION)
            && !player.hasPermission(WILDCARD_PERMISSION) && !player.isOp())
            return;

        // Check adjacent blocks to be sure an adjacent block wasn't broken either (e.g., A torch was broken due to its wall block being destroyed)
        for (HoneypotRegionObject honeypot : regionManager.getNearbyHoneypots(event.getBlock().getLocation(), 1)) {
            // If a break took place in a region, then there won't be any side effects.
            if (!honeypot.isSingleBlockRegion()) continue;

            Block block = honeypot.getPos1().getBlock();
            if (!block.getType().equals(Material.AIR)) continue;

            blockBreakEvent(new BlockBreakEvent(block, player));
            regionManager.deleteRegionContaining(block);
            logger.warning(Component.text(
                "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                    + block.getX() + ", " + block.getY() + ", " + block.getZ()
                    + ". " + player.getName()
                    + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot"));
        }
    }
}
