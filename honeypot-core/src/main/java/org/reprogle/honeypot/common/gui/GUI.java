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

package org.reprogle.honeypot.common.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.common.gui.button.GUIButton;
import org.reprogle.honeypot.common.gui.item.GUIItemBuilder;
import org.reprogle.honeypot.common.gui.menu.GUIMenuListener;
import org.reprogle.honeypot.common.gui.menu.GUIOpenMenu;
import org.reprogle.honeypot.common.gui.pagination.GUIPageButtonBuilder;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.util.ArrayList;
import java.util.List;

public class GUI {

	private final JavaPlugin plugin;

	private boolean blockDefaultInteractions = true;

	private boolean enableAutomaticPagination = true;

	private GUIPageButtonBuilder defaultPaginationButtonBuilder = (type, inventory) -> {
		switch (type) {
			case PREV_BUTTON -> {
				if (inventory.getCurrentPage() > 0)
					return new GUIButton(new GUIItemBuilder(
							Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("previous-page-item")))
							.name("&a&l← Previous Page")
							.lore("&aClick to move back to", "&apage " + inventory.getCurrentPage() + ".").build())
							.withListener(event -> {
								event.setCancelled(true);
								inventory.previousPage(event.getWhoClicked());
							});
				else
					return null;
			}
			case CURRENT_BUTTON -> {
				return new GUIButton(new GUIItemBuilder(
						Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("current-page-item")))
						.name("&7&lPage " + (inventory.getCurrentPage() + 1) + " of " + inventory.getMaxPage())
						.lore("&7You are currently viewing", "&7page " + (inventory.getCurrentPage() + 1) + ".")
						.build()).withListener(event -> event.setCancelled(true));
			}
			case NEXT_BUTTON -> {
				if (inventory.getCurrentPage() < inventory.getMaxPage() - 1)
					return new GUIButton(new GUIItemBuilder(
							Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("next-page-item")))
							.name("&a&lNext Page →")
							.lore("&aClick to move forward to", "&apage " + (inventory.getCurrentPage() + 2) + ".")
							.build()).withListener(event -> {
						event.setCancelled(true);
						inventory.nextPage(event.getWhoClicked());
					});
				else
					return null;
			}
			default -> {
				return null;
			}
		}
	};

	public GUI(JavaPlugin plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new GUIMenuListener(plugin, this), plugin);
	}

	public GUIMenu create(String name, int rows) {
		return create(name, rows, null);
	}

	public GUIMenu create(String name, int rows, String tag) {
		return new GUIMenu(plugin, this, name, rows, tag);
	}

	public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
		this.blockDefaultInteractions = blockDefaultInteractions;
	}

	public boolean areDefaultInteractionsBlocked() {
		return blockDefaultInteractions;
	}

	public void setEnableAutomaticPagination(boolean enableAutomaticPagination) {
		this.enableAutomaticPagination = enableAutomaticPagination;
	}

	public boolean isAutomaticPaginationEnabled() {
		return enableAutomaticPagination;
	}

	public void setDefaultPaginationButtonBuilder(GUIPageButtonBuilder defaultPaginationButtonBuilder) {
		this.defaultPaginationButtonBuilder = defaultPaginationButtonBuilder;
	}

	public GUIPageButtonBuilder getDefaultPaginationButtonBuilder() {
		return defaultPaginationButtonBuilder;
	}

	public List<GUIOpenMenu> findOpenWithTag(String tag) {

		List<GUIOpenMenu> foundInventories = new ArrayList<>();

		for (Player player : plugin.getServer().getOnlinePlayers()) {
			Inventory topInventory = player.getOpenInventory().getTopInventory();

			if (topInventory.getHolder() != null) {
				GUIMenu inventory = (GUIMenu) topInventory.getHolder();
				if (topInventory.getHolder() instanceof GUIMenu && inventory.getTag().equals(tag)) {
					foundInventories.add(new GUIOpenMenu(inventory, player));
				}
			}
		}

		return foundInventories;

	}

}
