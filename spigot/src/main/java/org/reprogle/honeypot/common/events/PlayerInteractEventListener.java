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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotPlayerInteractEvent;
import org.reprogle.honeypot.api.events.HoneypotPrePlayerInteractEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.util.List;
import java.util.Objects;

public class PlayerInteractEventListener implements Listener {

	/**
	 * Create a private constructor to hide the implicit one
	 */
	PlayerInteractEventListener() {

	}

	// Player interact event
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	@SuppressWarnings({ "unchecked", "java:S3776" })
	public static void playerInteractEvent(PlayerInteractEvent event) {

		if (event.getPlayer().getTargetBlockExact(5) == null)
			return;
		if (!(event.getPlayer().getTargetBlockExact(5).getState() instanceof Container))
			return;
		if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
			return;

		// We want to filter on inventories upon opening, not just creation (Like in the
		// HoneypotCreate class) because
		// inventories can be both broken AND open :)
		if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories"))) {
			List<String> allowedBlocks = (List<String>) HoneypotConfigManager.getPluginConfig()
					.getList("allowed-inventories");
			boolean allowed = false;

			for (String blockType : allowedBlocks) {
				if (Objects.requireNonNull(event.getPlayer().getTargetBlockExact(5)).getType().name()
						.equals(blockType)) {
					allowed = true;
					break;
				}
			}

			if (!allowed) {
				return;
			}
		}

		try {
			if (!Objects.requireNonNull(event.getPlayer().getTargetBlockExact(5)).getType().equals(Material.ENDER_CHEST)
					&& Boolean.TRUE.equals(HoneypotBlockManager.getInstance()
							.isHoneypotBlock(Objects.requireNonNull(event.getPlayer().getTargetBlockExact(5))))) {
				// Fire HoneypotPrePlayerInteractEvent
				HoneypotPrePlayerInteractEvent hppie = new HoneypotPrePlayerInteractEvent(event.getPlayer(),
						event.getClickedBlock());
				Bukkit.getPluginManager().callEvent(hppie);

				if (hppie.isCancelled())
					return;

				if (!(event.getPlayer().hasPermission("honeypot.exempt")
						|| event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())) {
					if (Boolean.FALSE.equals(
							HoneypotConfigManager.getPluginConfig().getBoolean("always-allow-container-access")))
						event.setCancelled(true);
					executeAction(event);
				}

				HoneypotPlayerInteractEvent hpie = new HoneypotPlayerInteractEvent(event.getPlayer(),
						event.getClickedBlock());
				Bukkit.getPluginManager().callEvent(hpie);
			}
		} catch (NullPointerException npe) {
			// Do nothing as it's most likely an entity. If this event is triggered, the
			// player will either be targeting
			// a block or entity, and there is no other option for it to be null.
		}
	}

	private static void executeAction(PlayerInteractEvent event) {

		Block block = event.getPlayer().getTargetBlockExact(5);

		assert block != null;
		String action = HoneypotBlockManager.getInstance().getAction(block);

		assert action != null;
		Honeypot.getHoneypotLogger().debug("PlayerInteractEvent being called for player: " + event.getPlayer().getName()
				+ ", UUID of " + event.getPlayer().getUniqueId() + ". Action is: " + action);

		ActionHandler.handleCustomAction(action, block, event.getPlayer());
	}

	/**
	 * This method is specifically for handling the debug functionality of Honeypot
	 * 
	 * @param event The event data being passed by the event handler
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public static void debugInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (player.getPersistentDataContainer().has(new NamespacedKey(Honeypot.plugin, "honeypot-debug-enabled"))
				&& event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);

			HoneypotBlockObject block = DataStoreManager.getInstance().getHoneypotBlock(event.getClickedBlock());
			if (block == null) {
				player.sendMessage(CommandFeedback.getChatPrefix() + " Not a Honeypot, no PDC found");
				return;
			}
			player.sendMessage(CommandFeedback.getChatPrefix() + " PDC contains: " + block.toString());
		}
	}
}
