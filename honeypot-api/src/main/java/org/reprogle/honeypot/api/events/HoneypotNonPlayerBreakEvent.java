package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is triggered after a Honeypot is edited by a non-player object.
 * This event is called <i>after</i> the block edit, not before.
 * This event is not cancellable, you cannot cancel the editing of a Honeypot moved by a non-object.
 * 
 * Currently this class can only return the Honeypot {@link Block} and a generic Object representing
 * the thing that attempted to edit the block.
 */
public class HoneypotNonPlayerBreakEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();

	private final Object object;

	private final Block block;

	/**
	 * Constructor for event
	 * @param object The object doing the breaking
	 * @param block The block being broken
	 */
	public HoneypotNonPlayerBreakEvent(Object object, Block block) {
		this.object = object;
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
	 * Get the object that triggered the event
	 * 
	 * @return Object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Get the block that was broken
	 * 
	 * @return {@link Block}
	 */
	public Block getBlock() {
		return block;
	}
}
