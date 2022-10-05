package org.reprogle.honeypot.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.utils.HoneypotConfigManager;

public class EntityChangeBlockEventListener implements Listener {

    /**
     * Create package constructor to hide implicit one
     */
    EntityChangeBlockEventListener() {

    }

    // Enderman event
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void entityChangeBlockEvent(EntityChangeBlockEvent event) {

        // If the entity grabbing the block is an enderman, if they are allowed to,
        // delete the
        // Honeypot, otherwise cancel it
        if (event.getEntity().getType().equals(EntityType.ENDERMAN)) {
            if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(event.getBlock()))) {

                Honeypot.getHoneypotLogger().log("EntityChangeBlockEvent being called for Honeypot: "
                        + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(),
                        event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-enderman"))) {
                    HoneypotBlockManager.getInstance().deleteBlock(event.getBlock());
                } else {
                    event.setCancelled(true);
                }
            }
        } else if (event.getEntity().getType().equals(EntityType.SILVERFISH)
                && Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(event.getBlock()))) {

            // Fire HoneypotNonPlayerBreakEvent
            HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hnpbe);

            event.setCancelled(true);
        }
    }
}
