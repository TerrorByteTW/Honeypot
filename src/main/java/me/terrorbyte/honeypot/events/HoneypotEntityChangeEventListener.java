package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class HoneypotEntityChangeEventListener implements Listener {

    //Enderman event
    @EventHandler(priority = EventPriority.LOW)
    public static void EntityChangeBlockEvent(EntityChangeBlockEvent event){

        //If the entity grabbing the block is an enderman, if they are allowed to, delete the Honeypot, otherwise cancel it
        if (event.getEntity().getType().equals(EntityType.ENDERMAN)){
            if(Honeypot.getPlugin().getConfig().getBoolean("allow-enderman")){
                HoneypotBlockStorageManager.deleteBlock(event.getBlock());
            } else {
                event.setCancelled(true);
            }
        }
    }

}
