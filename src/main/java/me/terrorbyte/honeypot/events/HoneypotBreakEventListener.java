package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.CustomBlockData;
import me.terrorbyte.honeypot.Honeypot;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HoneypotBreakEventListener implements Listener {

    @EventHandler
    public static void BlockBreakEvent(BlockBreakEvent event) {
        final PersistentDataContainer customBlockData = new CustomBlockData(event.getBlock(), Honeypot.getPlugin());
        final NamespacedKey storedItemKey = new NamespacedKey(Honeypot.getPlugin(), "honeypot");

        if(customBlockData.has(storedItemKey, PersistentDataType.INTEGER) && customBlockData.get(storedItemKey, PersistentDataType.INTEGER).equals(1)){
            event.setCancelled(true);
        }

    }
}
