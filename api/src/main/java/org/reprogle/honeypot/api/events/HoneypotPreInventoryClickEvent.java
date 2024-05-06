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

package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * Event that is triggered when a player triggers a Honeypot via interacting with its container inventory.
 * This event is called <i>before</i> the action is taken, not after.
 * This event is cancellable. If cancelled, the action is not taken.
 */
public class HoneypotPreInventoryClickEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean isCancelled;

	private final Player player;

	private final Inventory inv;

	/**
	 * Called before an action is ran on a player who modified a Honeypot inventory.
	 * If cancelled, the modification is allowed and action isn't taken
	 *
	 * @param player The Player breaking with the Honeypot
	 * @param inv    The inventory of the block
	 */
	public HoneypotPreInventoryClickEvent(Player player, Inventory inv) {
		this.player = player;
		this.inv = inv;
	}

	/**
	 * Boilerplate function for Bukkit
	 *
	 * @return HandlerList
	 */
	@Override
	public HandlerList getHandlers() {
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

	/**
	 * Check if the event is cancelled
	 *
	 * @return True if cancelled, false if not
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * Set the event as cancelled or not
	 *
	 * @param cancel Boolean value notating if the event is cancelled or not
	 */
	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
}
