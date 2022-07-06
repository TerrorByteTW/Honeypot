package org.reprogle.honeypot.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.api.events.HoneypotNonPlayerBreakEvent;

public class EntityChangeEventListener implements Listener {

    /**
     * Create package constructor to hide implicit one
     */
    EntityChangeEventListener() {

    }

    // Enderman event
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void entityChangeBlockEvent(EntityChangeBlockEvent event) {

        // If the entity grabbing the block is an enderman, if they are allowed to, delete the
        // Honeypot, otherwise cancel it
        if (event.getEntity().getType().equals(EntityType.ENDERMAN)) {
            if (Boolean.TRUE.equals(Honeypot.getHBM().isHoneypotBlock(event.getBlock()))) {

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(),
                        event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-enderman"))) {
                    Honeypot.getHBM().deleteBlock(event.getBlock());
                }
                else {
                    event.setCancelled(true);
                }
            }
        }
        else if (event.getEntity().getType().equals(EntityType.SILVERFISH)
                && Boolean.TRUE.equals(Honeypot.getHBM().isHoneypotBlock(event.getBlock()))) {

            // Fire HoneypotNonPlayerBreakEvent
            HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hnpbe);

            event.setCancelled(true);
        }
    }
}
