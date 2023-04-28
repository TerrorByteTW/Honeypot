package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * Event that is triggered before a Honeypot is created.
 * This event is called <i>before</i> the block is created, not after.
 * This event is cancellable. If cancelled, the creation does not happen.
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
}
