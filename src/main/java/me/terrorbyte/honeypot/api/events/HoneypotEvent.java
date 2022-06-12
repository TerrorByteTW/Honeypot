package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class HoneypotEvent extends Event {

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
	
}
