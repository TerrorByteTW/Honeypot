package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is triggered each time a Honeypot inventory is interacted with.
 * This event is called <i>after</i> the interaction is triggered, not before.
 * 
 * This event is not cancellable. If you need to cancel it, use {@link HoneypotPrePlayerInteractEvent}.
 */
public class HoneypotPlayerInteractEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;

	private final Block block;

	/**
	 * Called after action is taken on a player who interacted with the Honeypot. Non-cancellable
	 * 
	 * @param player The Player who broke with the Honeypot
	 * @param block The Honeypot block
	 */
	public HoneypotPlayerInteractEvent(Player player, Block block) {
		this.player = player;
		this.block = block;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	@SuppressWarnings("java:S4144")
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}

	public Block getBlock() {
		return block;
	}

}
