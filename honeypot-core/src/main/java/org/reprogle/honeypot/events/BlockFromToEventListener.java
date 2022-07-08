package org.reprogle.honeypot.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.reprogle.honeypot.Honeypot;

public class BlockFromToEventListener implements Listener{

	  /**
     * Create package listener to hide implicit one
     */
    BlockFromToEventListener() {

    }

    /**
     * Block water from flowing into Honeypot blocks (Such as torches)
     * @param event The BlockFromToEvent, passed from Bukkit's event handler
     */
    @EventHandler(priority = EventPriority.LOW)
    public static void blockFromToEvent(BlockFromToEvent event) {
      Block toBlock = event.getToBlock();
      if (Honeypot.getHBM().isHoneypotBlock(toBlock)) {
        Honeypot.getHoneypotLogger().log("BlockFromToEvent being called for Honeypot: " + toBlock.getX() + ", " + toBlock.getY() + ", " + toBlock.getZ());
        event.setCancelled(true);
      }
    }
	
}
