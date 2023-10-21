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

import org.bukkit.plugin.Plugin;
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
		plugin.getServer().getPluginManager().registerEvents(new BlockBreakEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new BlockFromToEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new BlockBurnEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new EntityChangeBlockEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new EntityExplodeEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PistonExtendRetractListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new InventoryMoveItemEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new StructureGrowEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerCommandPreprocessEventListener(), plugin);

		// A tiny bit of logic to register the proper container listeners
		if (Boolean.TRUE.equals(
				HoneypotConfigManager.getPluginConfig().getBoolean("container-actions.enable-container-actions"))) {
			if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig()
					.getBoolean("container-actions.use-inventory-click"))) {
				Honeypot.getHoneypotLogger().info("Using inventory click for containers");
				plugin.getServer().getPluginManager().registerEvents(new InventoryClickDragEventListener(), plugin);
			} else {
				Honeypot.getHoneypotLogger().info("Using player interact for containers");
				plugin.getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(), plugin);
			}
		}
	}

}
