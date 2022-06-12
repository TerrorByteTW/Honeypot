package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class HoneypotPrePlayerBreakEvent extends HoneypotCancellableEvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean isCancelled;

	private final Player PLAYER;
	private final Block BLOCK;

	/**
	 * Called before action is taken on a player who broke with Honeypot inventory. If cancelled, the Honeypot is ignored
	 * 
	 * @param player The Player breaking with the Honeypot
	 * @param block The Honeypot block
	 */
	public HoneypotPrePlayerBreakEvent(Player player, Block block){
		this.PLAYER = player;
		this.BLOCK = block;
	}

	public HandlerList getHandlerList() {
        return HANDLERS;
    }

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return PLAYER;
	}

	public Block getBlock() {
		return BLOCK;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
	
}