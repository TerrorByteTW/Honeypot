package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class HoneypotPlayerInteractEvent extends HoneypotEvent {
	private static final HandlerList HANDLERS = new HandlerList();


	private final Player PLAYER;
	private final Block BLOCK;

	/**
	 * Called after action is taken on a player who interacted with the Honeypot. Non-cancellable
	 * 
	 * @param player The Player who broke with the Honeypot
	 * @param block The Honeypot block
	 */
	public HoneypotPlayerInteractEvent(Player player, Block block){
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

}
