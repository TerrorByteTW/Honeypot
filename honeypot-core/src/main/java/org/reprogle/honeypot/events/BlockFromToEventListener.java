package org.reprogle.honeypot.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.reprogle.honeypot.Honeypot;

public class BlockFromToEventListener implements Listener{

	/**
     * Create package listener to hide implicit one
     */
    BlockFromToEventListener() {

    }

    @EventHandler
    public static void blockFromToEvent(BlockFromToEvent event) {
      Block toBlock = event.getToBlock();
	  if (Honeypot.getHBM().isHoneypotBlock(toBlock)) {
		event.setCancelled(true);
	  }
    }
	
}
