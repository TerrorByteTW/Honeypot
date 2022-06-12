package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class HoneypotCancellableEvent extends Event implements Cancellable {

	public abstract HandlerList getHandlerList();

	@Override
	public abstract HandlerList getHandlers();

	/**
	 * Returns the player involved in the event
	 * @return Player object of the player who triggered the event
	 */
	public abstract Player getPlayer();

	/**
	 * Returns the Honeypot broken
	 * @return Honeypot block object
	 */
	public abstract Block getBlock();

	/** 
	 * Sets the event as cancelled or not
	 * @param cancel Sets the event's cancelled state as whatever is passed
	 */
	public abstract void setCancelled(boolean cancel);

	/**
	 * Checks if the event is cancelled
	 * @return True if cancelled, false if not
	 */
	public abstract boolean isCancelled();
	
}
