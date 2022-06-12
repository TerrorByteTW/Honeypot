package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.api.events.HoneypotNonPlayerBreakEvent;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

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
            if (Boolean.TRUE.equals(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock()))) {

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(),
                        event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-enderman"))) {
                    HoneypotBlockStorageManager.deleteBlock(event.getBlock());
                }
                else {
                    event.setCancelled(true);
                }
            }
        }
        else if (event.getEntity().getType().equals(EntityType.SILVERFISH)
                && Boolean.TRUE.equals(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock()))) {

            // Fire HoneypotNonPlayerBreakEvent
            HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hnpbe);

            event.setCancelled(true);
        }
    }
}
