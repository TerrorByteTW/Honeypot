package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
