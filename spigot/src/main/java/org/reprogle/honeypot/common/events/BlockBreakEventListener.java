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

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPrePlayerBreakEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.discord.DiscordWebhookNotifier;
import org.reprogle.honeypot.common.utils.discord.WebhookActionType;

public class BlockBreakEventListener implements Listener {

    private static final String BREAK_PERMISSION = "honeypot.break";

    private static final String WILDCARD_PERMISSION = "honeypot.*";

    private static final String EXEMPT_PERMISSION = "honeypot.exempt";

    private final ActionHandler actionHandler;
    private final HoneypotLogger logger;
    private final HoneypotBlockManager blockManager;
    private final HoneypotConfigManager configManager;
    private final CommandFeedback commandFeedback;
    private final HoneypotPlayerHistoryManager playerHistoryManager;
    private final HoneypotPlayerManager playerManager;

    /**
     * Create a private constructor to hide the implicit one
     */
    @Inject
    public BlockBreakEventListener(ActionHandler actionHandler, HoneypotLogger logger, HoneypotBlockManager blockManager,
                                   HoneypotConfigManager configManager, CommandFeedback commandFeedback,
                                   HoneypotPlayerHistoryManager playerHistoryManager, HoneypotPlayerManager playerManager) {
        this.actionHandler = actionHandler;
        this.logger = logger;
        this.blockManager = blockManager;
        this.configManager = configManager;
        this.commandFeedback = commandFeedback;
        this.playerHistoryManager = playerHistoryManager;
        this.playerManager = playerManager;
    }
    // TODO - Integration GriefPrevention to ensure action isn't taken when blocks broken are inside GP regions

    // Player block break event
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings({"java:S3776", "java:S1192"})
    public void blockBreakEvent(BlockBreakEvent event) {
        // Check to see if the event is cancelled before doing any logic.
        // Ex: Creative mode player with Sword in hand
        if (event.isCancelled())
            return;
        if (blockManager.isHoneypotBlock(event.getBlock())) {
            // Fire HoneypotPrePlayerBreakEvent
            HoneypotPrePlayerBreakEvent hppbe = new HoneypotPrePlayerBreakEvent(event.getPlayer(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hppbe);
            logger.debug("Pre block break event is being called for " + event.getPlayer());

            // Check if the event was cancelled. If it is, delete the block.
            if (hppbe.isCancelled()) {
                blockManager.deleteBlock(event.getBlock());
                logger.debug("The event for " + event.getPlayer() + " was cancelled, not continuing.");
                return;
            }

            // Create a boolean for if we should remove the block from the DB or not
            boolean deleteBlock = false;

            // If Allow Player Destruction is true, the player has permissions, or is Op,
            // flag the block for deletion from the DB
            // Otherwise, set the BlockBreakEvent to cancelled
            if (configManager.getPluginConfig().getBoolean("allow-player-destruction")
                    || event.getPlayer().hasPermission(BREAK_PERMISSION)
                    || event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp()) {
                deleteBlock = true;
            } else {
                event.setCancelled(true);
                logger.debug(
                        "The player who broke this block is either allowed to break it, or has some sort of permission. This Honeypot will be removed from the world");
            }

            // If blocks broken before action is less than or equal to 1, go to the break
            // action.
            // Otherwise, check if the player should have the action triggered
            if (configManager.getPluginConfig().getInt("blocks-broken-before-action-taken") <= 1) {
                // This is just a precaution to ensure that the player count is always less than
                // 1 if the value in the
                // config is 1

                playerManager.setPlayerCount(event.getPlayer(), 0);
                logger.debug("Action is being taken for this event.");
                breakAction(event);
            } else {
                logger.debug("The break is just being counted, no action is taken yet");
                countBreak(event);
            }

            // Fire HoneypotPlayerBreakEvent
            HoneypotPlayerBreakEvent hpbe = new HoneypotPlayerBreakEvent(event.getPlayer(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hpbe);

            // If we flagged the block for deletion, remove it from the DB. Do this after
            // other actions have been
            // completed, otherwise the other actions will fail with NPEs
            if (deleteBlock) {
                blockManager.deleteBlock(event.getBlock());
            }
        }
    }

    // This is a separate event from the one above. We want to know if any Honeypots
    // were broken due to breaking a
    // supporting block, such as torches breaking due to
    // the block they're on being broken
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("java:S1192")
    public void checkBlockBreakSideEffects(BlockBreakEvent event) {
        if (!configManager.getPluginConfig().getBoolean("allow-player-destruction"))
            return;
        if (!event.getPlayer().hasPermission(BREAK_PERMISSION)
                && !event.getPlayer().hasPermission(WILDCARD_PERMISSION) && !event.getPlayer().isOp())
            return;

        Block block = event.getBlock();

        Block[] adjacentBlocks = new Block[]{block.getRelative(BlockFace.DOWN), block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.WEST), block.getRelative(BlockFace.UP)};

        // Check all the blocks on the *side* first
        for (Block adjacentBlock : adjacentBlocks) {
            if (!adjacentBlock.getType().equals(Material.AIR)) continue;

            if (blockManager.isHoneypotBlock(adjacentBlock)) {
                blockBreakEvent(new BlockBreakEvent(adjacentBlock, event.getPlayer()));
                blockManager.deleteBlock(adjacentBlock);
                logger.warning(
                        "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                                + adjacentBlock.getX() + ", " + adjacentBlock.getY() + ", " + adjacentBlock.getZ()
                                + ". " + event.getPlayer().getName()
                                + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
            }
        }
    }

    @SuppressWarnings({"java:S3776", "java:S2629", "java:S1192"})
    private void breakAction(BlockBreakEvent event) {

        // Get the block broken and the chat prefix for prettiness
        Block block = event.getBlock();

        // If the player isn't exempt, doesn't have permissions, and isn't Op
        if (!event.getPlayer().hasPermission(EXEMPT_PERMISSION) && !event.getPlayer().hasPermission(BREAK_PERMISSION)
                && !event.getPlayer().hasPermission(WILDCARD_PERMISSION) && !event.getPlayer().isOp()) {

            // Log the event in the history table
            playerHistoryManager.addPlayerHistory(event.getPlayer(),
                    blockManager.getHoneypotBlock(event.getBlock()), "break");

            // Grab the action from the block via the storage manager
            String action = blockManager.getAction(block);

            // Run certain actions based on the action of the Honeypot Block
            assert action != null;
            actionHandler.handleCustomAction(action, block, event.getPlayer());

            sendWebhook(event);

            // At this point we know the player has one of those permissions above. Now we
            // need to figure out which
        } else if (event.getPlayer().hasPermission(BREAK_PERMISSION)
                || event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("staff-broke"));

            // If it got to here, then they are exempt but can't break blocks anyway.
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("exempt-no-break"));
        }
    }

    private void countBreak(BlockBreakEvent event) {

        // Don't count the break if they are exempt, have the remove permission,
        // wildcard permission, or are Op
        if (event.getPlayer().hasPermission(EXEMPT_PERMISSION) || event.getPlayer().isOp()
                || event.getPlayer().hasPermission(BREAK_PERMISSION)
                || event.getPlayer().hasPermission(WILDCARD_PERMISSION))
            return;

        // Get the config value and the amount of blocks broken
        int breaksBeforeAction = configManager.getPluginConfig().getInt("blocks-broken-before-action-taken");
        int blocksBroken = playerManager.getCount(event.getPlayer());

        // getCount returns -1 if the player doesn't exist in the DB. If that's the
        // case, add the player to the DB
        if (blocksBroken == -1) {
            playerManager.addPlayer(event.getPlayer(), 0);
            blocksBroken = 0;
        }

        // Increment the blocks broken counter
        blocksBroken += 1;

        // If the blocks broken are larger than or equals 'breaks before action' or if
        // breaks before action equal equal 1, reset the count and perform the break
        if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
            playerManager.setPlayerCount(event.getPlayer(), 0);
            breakAction(event);
        } else {
            // Just count it
            playerManager.setPlayerCount(event.getPlayer(), blocksBroken);
            // Log the event in the history table
            playerHistoryManager.addPlayerHistory(event.getPlayer(),
                    blockManager.getHoneypotBlock(event.getBlock()), "prelimBreak");
            logger.debug("BlockBreakEvent being called for player: " + event.getPlayer().getName()
                    + ", UUID of " + event.getPlayer().getUniqueId() + ".");

            sendWebhook(event);
        }
    }

    private void sendWebhook(BlockBreakEvent event) {
        if (configManager.getPluginConfig().getBoolean("discord.enable")) {
            WebhookActionType actionType;
            String sendWhen = configManager.getPluginConfig().getString("discord.send-when");
            actionType = sendWhen.equalsIgnoreCase("onbreak") ? WebhookActionType.BREAK : WebhookActionType.ACTION;

            new DiscordWebhookNotifier(actionType, configManager.getPluginConfig().getString("discord.url"), event.getBlock(), event.getPlayer(), logger).send();
        }
    }
}
