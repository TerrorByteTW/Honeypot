package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class PistonMoveListener implements Listener {

    /**
     * Create private constructor to hide the implicit one
     */
    PistonMoveListener() {

    }

    // Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void pistonPushEvent(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block b : blocks) {
            if (Boolean.TRUE.equals(HoneypotBlockStorageManager.isHoneypotBlock(b))) {

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
            if (Boolean.TRUE.equals(HoneypotBlockStorageManager.isHoneypotBlock(b))) {

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getBlock(), event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                event.setCancelled(true);
                break;
            }
        }
    }
}
