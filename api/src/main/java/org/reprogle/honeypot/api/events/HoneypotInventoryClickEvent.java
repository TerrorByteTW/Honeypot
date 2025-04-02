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

package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered after a player triggers a container action
 * This event is called <i>after</i> a container action is triggered, not before. This means the state of the player may be unknown, for example if they were kicked as a result.
 * This event is not cancellable, you cannot cancel the action of a Honeypot container already interacted with by a player. If you need to cancel it, please use {@link HoneypotPreInventoryClickEvent}
 */
public class HoneypotInventoryClickEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;

	private final Inventory inv;

	/**
	 * Called after an action is ran on a player who modified a Honeypot inventory.
	 *
	 * @param player The Player breaking with the Honeypot
	 * @param inv    The inventory of the block
	 */
	public HoneypotInventoryClickEvent(Player player, Inventory inv) {
		this.player = player;
		this.inv = inv;
	}

	/**
	 * Boilerplate function for Bukkit
	 *
	 * @return HandlerList
	 */
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	/**
	 * Boilerplate function for Bukkit
	 *
	 * @return HandlerList
	 */
	@SuppressWarnings("java:S4144")
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	/**
	 * Get the player that broke the block
	 *
	 * @return {@link Player}
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Get the block that was involved in the event
	 *
	 * @return {@link Block}
	 */
	public Inventory getInventory() {
		return inv;
	}
}
