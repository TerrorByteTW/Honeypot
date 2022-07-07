package org.reprogle.honeypot.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsEventListener implements Listener {

    /**
     * Create package listener to hide implicit one
     */
    BlockPhysicsEventListener() {

    }

    // Is this even the best option? BlockPhysicsEvent is horribly bad...
    @EventHandler
    public static void blockPhysicsEvent(BlockPhysicsEvent event) {
      // TODO document why this method is empty
    }
    
}
