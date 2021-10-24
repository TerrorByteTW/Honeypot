package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class HoneypotPistonMoveListener implements Listener {

    //Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void PistonPushEvent(BlockPistonExtendEvent event) {
        if(event.isSticky()){
            List<Block> blocks = event.getBlocks();
            for (Block b : blocks){
                if(HoneypotBlockStorageManager.isHoneypotBlock(b)){
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public static void PistonPullEvent(BlockPistonRetractEvent event){
        if(event.isSticky()){
            List<Block> blocks = event.getBlocks();
            for (Block b : blocks){
                if(HoneypotBlockStorageManager.isHoneypotBlock(b)){
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
