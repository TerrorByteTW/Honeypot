package org.reprogle.honeypot.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;

import java.util.List;

public class PistonExtendRetractListener implements Listener {

    /**
     * Create private constructor to hide the implicit one
     */
    PistonExtendRetractListener() {

    }

    // Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void pistonPushEvent(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block b : blocks) {
            if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
                Honeypot.getHoneypotLogger().log(
                        "PistonExtendEvent being called for Honeypot: " + b.getX() + ", " + b.getY() + "," + b.getZ());

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getBlock(), event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public static void pistonPullEvent(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block b : blocks) {
            if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
                Honeypot.getHoneypotLogger().log("PistonRetractEvent being called for Honeypot: " + b.getX() + ", "
                        + b.getY() + ", " + b.getZ());

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getBlock(), event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                event.setCancelled(true);
                break;
            }
        }
    }
}
