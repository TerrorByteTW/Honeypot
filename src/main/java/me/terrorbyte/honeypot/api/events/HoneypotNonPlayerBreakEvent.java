package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HoneypotNonPlayerBreakEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();


	private final Object OBJECT;
	private final Block BLOCK;

	public HoneypotNonPlayerBreakEvent(Object object, Block block){
		this.OBJECT = object;
		this.BLOCK = block;
	}

	public static HandlerList getHandlerList() {
        return HANDLERS;
    }

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Object getObject() {
		return OBJECT;
	}

	public Block getBlock() {
		return BLOCK;
	}
}
