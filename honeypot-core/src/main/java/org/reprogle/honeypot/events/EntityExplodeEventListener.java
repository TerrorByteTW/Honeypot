package org.reprogle.honeypot.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityExplodeEventListener implements Listener {

    /**
     * Create package constructor to hide implicit one
     */
    EntityExplodeEventListener(){

    }

    // Explosion listener
    @EventHandler(priority = EventPriority.LOW)
    public static void entityExplodeEvent(EntityExplodeEvent event) {
        // Get every block that would've been blown up
        List<Block> destroyedBlocks = event.blockList();
        ArrayList<Block> foundHoneypotBlocks = new ArrayList<>();
        boolean allowExplosions = HoneypotConfigManager.getPluginConfig().getBoolean("allow-explode");
        Entity e = event.getEntity(); 
        Entity source = null;

        if (e instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) e;
            source = tnt.getSource();
        }

        // For every block, check if it was a Honeypot. If it was, check if explosions are allowed.
        // If so, just delete the Honeypot. If not, cancel the explosion
        for (Block block : destroyedBlocks) {
            if (Boolean.TRUE.equals(Honeypot.getBlockManager().isHoneypotBlock(block))) {
                Honeypot.getHoneypotLogger().log("EntityExplodeEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ());

                if (source instanceof Player) {
                    Honeypot.getPlayerHistoryManager().addPlayerHistory((Player) source, Honeypot.getBlockManager().getHoneypotBlock(block));
                    Honeypot.getHoneypotLogger().log("EntityExplodeEvent was caused by a player! It has been logged in the history. Player was: " + ((Player) source).getName());
                }

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), block);
                Bukkit.getPluginManager().callEvent(hnpbe);

                if (allowExplosions) {
                    Honeypot.getBlockManager().deleteBlock(block);
                }
                else {
                    foundHoneypotBlocks.add(block);
                }
            }
        }

        destroyedBlocks.removeAll(foundHoneypotBlocks);

    }

}
