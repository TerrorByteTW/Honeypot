package org.reprogle.honeypot.common.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;

public class LeavesDecayEventListener implements Listener {

    /**
     * Create package listener to hide implicit one
     */
    LeavesDecayEventListener() {

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onLeavesDecayEvent(LeavesDecayEvent event) {
        Block block = event.getBlock();

        if (HoneypotBlockManager.getInstance().isHoneypotBlock(block)) {
            Honeypot.getHoneypotLogger().debug("LeavesDecayEvent being called for Honeypot: " + block.getX() + ", "
                    + block.getY() + ", " + block.getZ());
            event.setCancelled(true);
        }
    }

}
