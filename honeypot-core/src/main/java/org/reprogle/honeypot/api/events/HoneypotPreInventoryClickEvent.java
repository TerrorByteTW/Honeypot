package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * Event that is triggered before a Honeypot is created.
 * This event is called <i>before</i> the block is created, not after.
 * This event is cancellable. If cancelled, the creation does not happen.
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
