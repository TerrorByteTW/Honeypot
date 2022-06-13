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

	public HoneypotNonPlayerBreakEvent(Object object, Block block) {
		this.object = object;
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

	public Object getObject() {
		return object;
	}

	public Block getBlock() {
		return block;
	}
}
