package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class SignChangeEventListener implements Listener {

    private final HoneypotBlockManager blockManager;
    private final HoneypotLogger logger;

    /**
     * Create package listener to hide implicit one
     */
    @Inject
    SignChangeEventListener(HoneypotBlockManager blockManager, HoneypotLogger logger) {
        this.blockManager = blockManager;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {
        Block block = event.getBlock();

        if (blockManager.isHoneypotBlock(block)) {
            logger.debug("SignChangeEvent being called for Honeypot: " + block.getX() + ", "
                    + block.getY() + ", " + block.getZ());
            event.setCancelled(true);
        }
    }

}
