package me.terrorbyte.honeypot.api;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HoneypotNonPlayerBreakEvent extends Event implements Cancellable{
	private static final HandlerList HANDLERS = new HandlerList();

	private boolean isCancelled;

	private final Object object;
	private final Block block;

	public HoneypotNonPlayerBreakEvent(Object object, Block block){
		this.object = object;
		this.block = block;
	}

	public static HandlerList getHandlerList() {
        return HANDLERS;
    }

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Object getObject() {
		return object;
	}

	public Block getBlock() {
		return block;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
}
