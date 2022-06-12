package me.terrorbyte.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class HoneypotCreateEvent extends HoneypotEvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player PLAYER;
	private final Block BLOCK;

	public HoneypotCreateEvent(Player player, Block block){
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
