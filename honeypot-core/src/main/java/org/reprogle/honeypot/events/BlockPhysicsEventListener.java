package org.reprogle.honeypot.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.util.PhysicsBlocks;

public class BlockPhysicsEventListener implements Listener {
  /**
   * Create package listener to hide implicit one
   */
  BlockPhysicsEventListener() {

  }

  // Is this even the best option? BlockPhysicsEvent is very inefficient...
  // One way to improve efficiency is to check if the changed block material is included in a list of blocks known to be affected by physics
  @EventHandler(priority = EventPriority.LOW)
  public static void blockPhysicsEvent(BlockPhysicsEvent event) {
    PhysicsBlocks pb = new PhysicsBlocks();
    if(!pb.getPhysicsSide().contains(event.getChangedType()) && !pb.getPhysicsUp().contains(event.getChangedType())) return;
    if (Honeypot.getHBM().isHoneypotBlock(event.getBlock())) {
      Honeypot.getHBM().deleteBlock(event.getBlock());
    }
  }

}
