package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.api.HoneypotNonPlayerBreakEvent;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeEventListener implements Listener {

    //Enderman event
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void EntityChangeBlockEvent(EntityChangeBlockEvent event){

        //If the entity grabbing the block is an enderman, if they are allowed to, delete the Honeypot, otherwise cancel it
        if (event.getEntity().getType().equals(EntityType.ENDERMAN)){
            if(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock())) {

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                if (HoneypotConfigManager.getPluginConfig().getBoolean("allow-enderman")) {
                    HoneypotBlockStorageManager.deleteBlock(event.getBlock());
                } else {
                    event.setCancelled(true);
                }
            }
        } else if (event.getEntity().getType().equals(EntityType.SILVERFISH)) {
            if(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock())){

                // Fire HoneypotNonPlayerBreakEvent
                HoneypotNonPlayerBreakEvent hnpbe = new HoneypotNonPlayerBreakEvent(event.getEntity(), event.getBlock());
                Bukkit.getPluginManager().callEvent(hnpbe);

                event.setCancelled(true);
            }
        }
    }
}
