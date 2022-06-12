package me.terrorbyte.honeypot.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HoneypotPlayerBreakEvent extends Event{
	
	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final Block block;

	public HoneypotPlayerBreakEvent(Player player, Block block) {
		this.player = player;
		this.block = block;
	}

	public static HandlerList getHandlerList() {
        return HANDLERS;
    }

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}

	public Block getBlock() {
		return block;
	}

}
