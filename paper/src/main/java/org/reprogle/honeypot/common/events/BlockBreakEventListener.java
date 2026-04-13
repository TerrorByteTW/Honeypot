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
import org.reprogle.honeypot.api.events.HoneypotPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPrePlayerBreakEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.store.HoneypotBlockManager;
import org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.store.HoneypotPlayerManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.discord.DiscordWebhookNotifier;
import org.reprogle.honeypot.common.utils.discord.WebhookActionType;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

public class BlockBreakEventListener implements Listener, IHoneypotEvent {

    private static final String BREAK_PERMISSION = "honeypot.break";

    private static final String WILDCARD_PERMISSION = "honeypot.*";

    private static final String EXEMPT_PERMISSION = "honeypot.exempt";

    private final ActionHandler actionHandler;
    private final HoneypotLogger logger;
    private final HoneypotBlockManager blockManager;
    private final BytePluginConfig config;
    private final CommandFeedback commandFeedback;
    private final HoneypotPlayerHistoryManager playerHistoryManager;
    private final HoneypotPlayerManager playerManager;
    private final AdapterManager adapterManager;

    @Inject
    public BlockBreakEventListener(ActionHandler actionHandler, HoneypotLogger logger, HoneypotBlockManager blockManager,
                                   BytePluginConfig config, CommandFeedback commandFeedback,
                                   HoneypotPlayerHistoryManager playerHistoryManager, HoneypotPlayerManager playerManager, AdapterManager adapterManager) {
        this.actionHandler = actionHandler;
        this.logger = logger;
        this.blockManager = blockManager;
        this.config = config;
        this.commandFeedback = commandFeedback;
        this.playerHistoryManager = playerHistoryManager;
        this.playerManager = playerManager;
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
        if (!blockManager.isHoneypotBlock(event.getBlock())) return;

        // If any of the adapters state that this is a disallowed action, don't bother doing anything since it was already blocked
        // It really shouldn't be in this region anyway, so it's going to be removed
        if (!adapterManager.checkAllAdapters(player, event.getBlock().getLocation())) {
            blockManager.deleteBlock(event.getBlock());
            return;
        }

        // Fire HoneypotPrePlayerBreakEvent
        HoneypotPrePlayerBreakEvent hppbe = new HoneypotPrePlayerBreakEvent(player, event.getBlock());
        Bukkit.getPluginManager().callEvent(hppbe);
        logger.debug(Component.text("HoneypotPrePlayerBreakEvent is being called for " + player), true);

        // Check if the event was canceled. If it is, delete the block.
        if (hppbe.isCancelled()) {
            blockManager.deleteBlock(event.getBlock());
            logger.debug(Component.text("HoneypotPrePlayerBreakEvent for " + player + " was cancelled, not continuing."), true);
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
            logger.debug(Component.text("Player " + player + " is either allowed to break Honeypots or has some sort of permission. This Honeypot will be removed from the world"), false);
        } else {
            event.setCancelled(true);
        }

        // If "blocks-broken-before-action-taken" is less than or equal to 1, go to the break action.
        // Otherwise, check if the player should have the action triggered
        if (config.config().getInt("blocks-broken-before-action-taken") <= 1) {
            // This is just a precaution to ensure that the player count is always less than
            // 1 if the value in the
            // config is 1
            playerManager.setPlayerCount(player, 0);
            breakAction(event);
        } else {
            countBreak(event);
        }

        // Fire HoneypotPlayerBreakEvent
        HoneypotPlayerBreakEvent hpbe = new HoneypotPlayerBreakEvent(player, event.getBlock());
        Bukkit.getPluginManager().callEvent(hpbe);
        logger.debug(Component.text("HoneypotPlayerBreakEvent is being called for " + player), true);

        // If we flagged the block for deletion, remove it from the DB. Do this after
        // other actions have been
        // completed, otherwise the other actions will fail with NPEs
        if (deleteBlock) {
            logger.debug(Component.text("Block is flagged for deletion, removing it from storage"), true);
            blockManager.deleteBlock(event.getBlock());
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
        for (HoneypotBlockObject honeypot : blockManager.getNearbyHoneypots(event.getBlock().getLocation(), 1)) {
            Block block = honeypot.getBlock();
            if (!block.getType().equals(Material.AIR)) continue;

            blockBreakEvent(new BlockBreakEvent(block, player));
            blockManager.deleteBlock(block);
            logger.warning(Component.text(
                "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                    + block.getX() + ", " + block.getY() + ", " + block.getZ()
                    + ". " + player.getName()
                    + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot"));
        }
    }

    private void breakAction(BlockBreakEvent event) {

        logger.debug(Component.text("Action should be taken for this block, attempting to take action"), true);

        // Get the block broken and the chat prefix for prettiness
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Ensure that the player has no exemption and isn't allowed to bypass it. This means that the player:
        // 1. Cannot have `honeypot.exempt`, `honeypot.break`, or `honeypot.*`
        // 2. Cannot be /op
        if (!player.hasPermission(EXEMPT_PERMISSION) && !player.hasPermission(BREAK_PERMISSION)
            && !player.hasPermission(WILDCARD_PERMISSION) && !player.isOp()) {
            // Grab the action from the block via the storage manager
            String action = blockManager.getAction(block);

            if (action == null) {
                logger.debug(Component.text("A BlockBreakEvent was called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + ". However, the action was null, so this must be a FAKE HONEYPOT. Please investigate the block at " + block.getX() + ", " + block.getY() + ", " + block.getZ()), false);
                return;
            }

            // Log the event in the history table
            playerHistoryManager.addPlayerHistory(player,
                blockManager.getHoneypotBlock(event.getBlock()), "break");

            // Run certain actions based on the action of the Honeypot Block
            actionHandler.handle(action, block, player);
            logger.debug(Component.text("Action successfully taken for block " + block + " on player " + player + " via BlockBreakEvent"), false);

            sendWebhook(event);

            // At this point we know the player has one of those permissions above. Now we
            // need to figure out which
            return;
        } else if (player.hasPermission(BREAK_PERMISSION)
            || player.hasPermission(WILDCARD_PERMISSION) || player.isOp()) {
            logger.debug(Component.text("Action was not taken for this event, player " + player + " has permission to break Honeypots"), false);
            player.sendMessage(commandFeedback.sendCommandFeedback("staff-broke"));
            return;
        }

        // If it got to here, then they are exempt but don't explicitly have break privileges
        logger.debug(Component.text("Action was not taken for this event, the player is exempt from having action taken against them. However, they are not allowed to break Honeypots, so the block still exists."), false);
    }

    private void countBreak(BlockBreakEvent event) {
        logger.debug(Component.text("Attempting to count the break for player: " + event.getPlayer().getName() + ", UUID of " + event.getPlayer().getUniqueId() + "."), true);

        Player player = event.getPlayer();

        // Don't count the break if they are exempt, have the remove permission,
        // wildcard permission, or are Op
        if (player.hasPermission(EXEMPT_PERMISSION) || player.isOp()
            || player.hasPermission(BREAK_PERMISSION)
            || player.hasPermission(WILDCARD_PERMISSION)) {
            logger.debug(Component.text("This break was not counted, player " + player + " is either exempt or has permission to break Honeypots."), false);
            return;
        }

        // Get the config value and the number of blocks broken
        int breaksBeforeAction = config.config().getInt("blocks-broken-before-action-taken");
        int blocksBroken = playerManager.getCount(player);

        // getCount returns -1 if the player doesn't exist in the DB. If that's the
        // case, add the player to the DB
        if (blocksBroken == -1) {
            playerManager.addPlayer(player, 0);
            blocksBroken = 0;
        }

        // Increment the blocks broken counter
        blocksBroken += 1;

        // If the blocks broken are larger than or equals 'breaks before action' or if
        // breaks before action equal 1, reset the count and perform the break
        if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
            logger.debug(Component.text("Player " + player + " has crossed the blocks broken threshold, triggering action against them"), false);
            playerManager.setPlayerCount(player, 0);
            breakAction(event);
        } else {
            logger.debug(Component.text("Player " + player + " has not yet crossed the blocks broken threshold, break is being counted and no action is being taken"), false);
            // Just count it
            playerManager.setPlayerCount(player, blocksBroken);
            // Log the event in the history table
            playerHistoryManager.addPlayerHistory(player,
                blockManager.getHoneypotBlock(event.getBlock()), "prelimBreak");

            sendWebhook(event);
        }
    }

    private void sendWebhook(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (config.config().getBoolean("discord.enable")) {
            WebhookActionType actionType;
            String sendWhen = config.config().getString("discord.send-when");
            actionType = sendWhen.equalsIgnoreCase("onbreak") ? WebhookActionType.BREAK : WebhookActionType.ACTION;

            new DiscordWebhookNotifier(actionType, config.config().getString("discord.url"), event.getBlock(), player, logger).send();
        }
    }
}
