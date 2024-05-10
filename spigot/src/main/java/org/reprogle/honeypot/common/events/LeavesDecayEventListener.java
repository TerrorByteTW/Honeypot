package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class LeavesDecayEventListener implements Listener {

    private final HoneypotBlockManager honeypotBlockManager;
    private final HoneypotLogger logger;

    /**
     * Create package listener to hide implicit one
     */
    @Inject
    LeavesDecayEventListener(HoneypotBlockManager honeypotBlockManager, HoneypotLogger logger) {
        this.honeypotBlockManager = honeypotBlockManager;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLeavesDecayEvent(LeavesDecayEvent event) {
        Block block = event.getBlock();

        if (honeypotBlockManager.isHoneypotBlock(block)) {
            logger.debug("LeavesDecayEvent being called for Honeypot: " + block.getX() + ", "
                    + block.getY() + ", " + block.getZ());
            event.setCancelled(true);
        }
    }

}
