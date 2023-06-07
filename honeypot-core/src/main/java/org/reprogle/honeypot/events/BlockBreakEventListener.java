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

package org.reprogle.honeypot.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPrePlayerBreakEvent;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.utils.ActionHandler;
import org.reprogle.honeypot.utils.HoneypotConfigManager;
import org.reprogle.honeypot.utils.PhysicsUtil;

public class BlockBreakEventListener implements Listener {

	private static final String REMOVE_PERMISSION = "honeypot.break";

	private static final String WILDCARD_PERMISSION = "honeypot.*";

	private static final String EXEMPT_PERMISSION = "honeypot.exempt";

	/**
	 * Create a private constructor to hide the implicit one
	 */
	BlockBreakEventListener() {

	}

	// Player block break event
	@EventHandler(priority = EventPriority.LOW)
	@SuppressWarnings({"java:S3776", "java:S1192"})
	public static void blockBreakEvent(BlockBreakEvent event) {
		// Check to see if the event is cancelled before doing any logic.
		// Ex: Creative mode player with Sword in hand
		if (event.isCancelled())
			return;
		if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(event.getBlock()))) {
			// Fire HoneypotPrePlayerBreakEvent
			HoneypotPrePlayerBreakEvent hppbe = new HoneypotPrePlayerBreakEvent(event.getPlayer(), event.getBlock());
			Bukkit.getPluginManager().callEvent(hppbe);

			// Check if the event was cancelled. If it is, delete the block.
			if (hppbe.isCancelled()) {
				HoneypotBlockManager.getInstance().deleteBlock(event.getBlock());
				return;
			}

			// Create a boolean for if we should remove the block from the DB or not
			boolean deleteBlock = false;

			// If Allow Player Destruction is true, the player has permissions or is Op,
			// flag the block for deletion
			// from the DB. Otherwise, set the BlockBreakEvent to cancelled
			if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction"))
					|| (event.getPlayer().hasPermission(REMOVE_PERMISSION)
					|| event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp())) {
				deleteBlock = true;
			} else {
				event.setCancelled(true);
			}

			// If blocks broken before action is less than or equal to 1, or allow
			// destruction is enabled, just go to
			// the break action. Otherwise, count it
			if (HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken") <= 1 || Boolean.TRUE
					.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction"))) {
				breakAction(event);
			} else {
				// If the player is not exempt, not op, and does not have remove perms, count
				// the break. Otherwise, just
				// activate the break action.
				if (!event.getPlayer().hasPermission(EXEMPT_PERMISSION) && !event.getPlayer().isOp()
						&& !event.getPlayer().hasPermission(REMOVE_PERMISSION)) {
					countBreak(event);
				} else {
					breakAction(event);
				}
			}

			// Fire HoneypotPlayerBreakEvent
			HoneypotPlayerBreakEvent hpbe = new HoneypotPlayerBreakEvent(event.getPlayer(), event.getBlock());
			Bukkit.getPluginManager().callEvent(hpbe);

			// If we flagged the block for deletion, remove it from the DB. Do this after
			// other actions have been
			// completed, otherwise the other actions will fail with NPEs
			if (deleteBlock) {
				HoneypotBlockManager.getInstance().deleteBlock(event.getBlock());
			}
		}
	}

	// This is a separate event from the one above. We want to know if any Honeypots
	// were broken due to breaking a
	// "root" block, such as torches breaking due to
	// the block they're on being broken
	@EventHandler(priority = EventPriority.LOW)
	@SuppressWarnings("java:S1192")
	public static void checkBlockBreakSideEffects(BlockBreakEvent event) {
		if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction"))
				&& !(event.getPlayer().hasPermission(REMOVE_PERMISSION)
				|| event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp()))
			return;

		Block block = event.getBlock();

		Block[] adjacentBlocks = new Block[]{
				block.getRelative(BlockFace.DOWN),
				block.getRelative(BlockFace.NORTH),
				block.getRelative(BlockFace.SOUTH),
				block.getRelative(BlockFace.EAST),
				block.getRelative(BlockFace.WEST)
		};

		Block blockUp = block.getRelative(BlockFace.UP);

		// Check all the blocks on the *side* first
		for (Block adjacentBlock : adjacentBlocks) {
			if (!PhysicsUtil.getSidePhysics().contains(adjacentBlock.getType()))
				continue;

			if (HoneypotBlockManager.getInstance().isHoneypotBlock(adjacentBlock)) {
				blockBreakEvent(new BlockBreakEvent(adjacentBlock, event.getPlayer()));
				HoneypotBlockManager.getInstance().deleteBlock(adjacentBlock);
				Honeypot.plugin.getLogger().warning(
						"A Honeypot has been removed due to the block it's attached to being broken. It was located at "
								+ adjacentBlock.getX() + ", " + adjacentBlock.getY() + ", " + adjacentBlock.getZ()
								+ ". " + event.getPlayer().getName()
								+ " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
				Honeypot.getHoneypotLogger().log(
						"A Honeypot has been removed due to the block it's attached to being broken. It was located at "
								+ adjacentBlock.getX() + ", " + adjacentBlock.getY() + ", " + adjacentBlock.getZ()
								+ ". " + event.getPlayer().getName()
								+ " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
			}
		}

		// Now check the block on the top (This is important because there are less
		// blocks that can be anchored on the sides of blocks than on the top of blocks)
		if (PhysicsUtil.getUpPhysics().contains(blockUp.getType())
				&& HoneypotBlockManager.getInstance().isHoneypotBlock(blockUp)) {
			blockBreakEvent(new BlockBreakEvent(blockUp, event.getPlayer()));
			HoneypotBlockManager.getInstance().deleteBlock(blockUp);
			Honeypot.plugin.getLogger().warning(
					"A Honeypot has been removed due to the block it's attached to being broken. It was located at "
							+ blockUp.getX() + ", " + blockUp.getY() + ", " + blockUp.getZ()
							+ ". " + event.getPlayer().getName()
							+ " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
			Honeypot.getHoneypotLogger().log(
					"A Honeypot has been removed due to the block it's attached to being broken. It was located at "
							+ blockUp.getX() + ", " + blockUp.getY() + ", " + blockUp.getZ()
							+ ". " + event.getPlayer().getName()
							+ " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
		}
	}

	@SuppressWarnings({"java:S3776", "java:S2629", "java:S1192"})
	private static void breakAction(BlockBreakEvent event) {

		// Get the block broken and the chat prefix for prettiness
		Block block = event.getBlock();
		String chatPrefix = CommandFeedback.getChatPrefix();

		// If the player isn't exempt, doesn't have permissions, and isn't Op
		if (!(event.getPlayer().hasPermission(EXEMPT_PERMISSION) || event.getPlayer().hasPermission(REMOVE_PERMISSION)
				|| event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp())) {

			// Log the event in the history table
			HoneypotPlayerHistoryManager.getInstance().addPlayerHistory(event.getPlayer(),
					HoneypotBlockManager.getInstance().getHoneypotBlock(event.getBlock()));

			// Grab the action from the block via the storage manager
			String action = HoneypotBlockManager.getInstance().getAction(block);

			// Run certain actions based on the action of the Honeypot Block
			assert action != null;
			Honeypot.getHoneypotLogger().log("BlockBreakEvent being called for player: " + event.getPlayer().getName()
					+ ", UUID of " + event.getPlayer().getUniqueId() + ". Action is: " + action);
			switch (action) {
				case "kick", "ban", "warn", "notify" -> {
					Honeypot.processor.process(Honeypot.getRegistry().getBehaviorProvider(action), event.getPlayer(), null);
				}

				case "nothing" -> {
					// Do...nothing
				}

				default -> {
					ActionHandler.handleCustomAction(action, block, event.getPlayer());
				}
			}

			// At this point we know the player has one of those permissions above. Now we
			// need to figure out which
		} else if (event.getPlayer().hasPermission(REMOVE_PERMISSION)
				|| event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp()) {
			event.getPlayer().sendMessage(CommandFeedback.sendCommandFeedback("staffbroke"));

			// If it got to here, then they are exempt but can't break blocks anyway.
		} else {
			event.setCancelled(true);
			event.getPlayer().sendMessage(CommandFeedback.sendCommandFeedback("exemptnobreak"));
		}
	}

	private static void countBreak(BlockBreakEvent event) {

		// Get the config value and the amount of blocks broken
		int breaksBeforeAction = HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken");
		int blocksBroken = HoneypotPlayerManager.getInstance().getCount(event.getPlayer());

		// getCount returns -1 if the player doesn't exist in the DB. If that's the
		// case, add the player to the DB
		if (blocksBroken == -1) {
			HoneypotPlayerManager.getInstance().addPlayer(event.getPlayer(), 0);
			blocksBroken = 0;
		}

		// Increment the blocks broken counter
		blocksBroken += 1;

		// If the blocks broken are larger than or equals 'breaks before action' or if
		// breaks before action equal equals
		// 1,
		// reset the count and perform the break
		if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
			HoneypotPlayerManager.getInstance().setPlayerCount(event.getPlayer(), 0);
			breakAction(event);
		} else {
			// Just count it
			HoneypotPlayerManager.getInstance().setPlayerCount(event.getPlayer(), blocksBroken);
		}
	}
}
