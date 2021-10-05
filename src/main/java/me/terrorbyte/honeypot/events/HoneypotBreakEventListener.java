package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.storagemanager.HoneypotFileManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class HoneypotBreakEventListener implements Listener {

    @EventHandler
    public static void BlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();

        //TODO - Add support for block break actions
        if(HoneypotFileManager.isHoneypotBlock(block)){
            event.setCancelled(true);
        }

    }
}
