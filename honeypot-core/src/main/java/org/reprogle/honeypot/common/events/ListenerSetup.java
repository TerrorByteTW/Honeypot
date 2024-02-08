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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

public class ListenerSetup {

	/**
	 * Create package listener to hide implicit one
	 */
	ListenerSetup() {

	}

	/**
	 * Set's up all the listeners in the entire plugin
	 *
	 * @param plugin The Honeypot plugin instance
	 */
	public static void setupListeners(Plugin plugin) {

		// All primary listners go here
		final List<Listener> primaryListeners = new ArrayList<>(List.of(new BlockBreakEventListener(),
				new BlockFromToEventListener(), new BlockBurnEventListener(), new EntityChangeBlockEventListener(),
				new EntityExplodeEventListener(), new PistonExtendRetractListener(),
				new InventoryMoveItemEventListener(), new StructureGrowEventListener(),
				new PlayerCommandPreprocessEventListener(), new PlayerJoinEventListener()));

		// All secondary listeners here
		final List<Listener> secondaryListeners = new ArrayList<>(
				List.of(new BlockFormEventListener(), new LeavesDecayEventListener(), new SignChangeEventListener()));

		// Initial registration of events
		PluginManager manager = plugin.getServer().getPluginManager();
		primaryListeners.forEach(event -> manager.registerEvents(event, plugin));

		// Register the proper events for container actions and their processors
		if (Boolean.TRUE.equals(
				HoneypotConfigManager.getPluginConfig().getBoolean("container-actions.enable-container-actions"))) {
			if (Boolean.TRUE.equals(
					HoneypotConfigManager.getPluginConfig().getBoolean("container-actions.use-inventory-click"))) {
				Honeypot.getHoneypotLogger().info("Using inventory click for containers");
				manager.registerEvents(new InventoryClickDragEventListener(), plugin);
			}
			else {
				Honeypot.getHoneypotLogger().info("Using player interact for containers");
				manager.registerEvents(new PlayerInteractEventListener(), plugin);
			}
		}

		// Register extra unnecessary events
		if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-extra-events"))) {
			Honeypot.getHoneypotLogger().info(
					"Extra events have been enabled. These shouldn't cause lag, but do note they may fire without player interaction necessary");
			secondaryListeners.forEach(event -> manager.registerEvents(event, plugin));
		}
	}

}