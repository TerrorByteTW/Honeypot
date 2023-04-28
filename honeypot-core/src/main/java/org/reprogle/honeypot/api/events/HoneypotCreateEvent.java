package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is triggered each time a Honeypot is created.
 * This event is called <i>after</i> the event is triggered, not before.
 * This event is not cancellable, if you need to cancel it use {@link HoneypotPreCreateEvent}.
 */
public class HoneypotCreateEvent extends Event {

	/**
	 * The handlers list of the event.
	 * 
	 * @see HandlerList
	 */
	private static final HandlerList HANDLERS = new HandlerList();

	/**
	 * The {@link Player} who fires the event
	 */
	private final Player player;

	/**
	 * The {@link Block} involved the event
	 */
	private final Block block;

	/**
	 * Creates a Honeypot Event. This shouldn't be used by other plugins. Instead, listen for the event instead.
	 * @param player The {@link Player} who fired the event
	 * @param block The {@link Block} involved in the event
	 * 
	 * @see #getPlayer()
	 * @see #getBlock()
	 */
	public HoneypotCreateEvent(Player player, Block block) {
		this.player = player;
		this.block = block;
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
	 * Get the player object of the player who fired the event
	 * @return {@link Player}
	 * @see #getBlock() #getBlock()
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Get the block object of the block who fired the event
	 * @return {@link Block}
	 * @see #getPlayer() #getPlayer()
	 */
	public Block getBlock() {
		return block;
	}
}
