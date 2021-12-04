package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class HoneypotExplosionEventListener implements Listener {

    //Explosion listener
    @EventHandler(priority = EventPriority.LOW)
    public static void EntityExplodeEvent(EntityExplodeEvent event){
        //Get every block that would've been blown up
        List<Block> destroyedBlocks = event.blockList();
        ArrayList<Block> foundHoneypotBlocks = new ArrayList<>();

        //For every block, check if it was a Honeypot. If it was, check if explosions are allowed. If so, just delete the Honeypot. If not, cancel the explosion
        for (Block block : destroyedBlocks) {
            if (HoneypotBlockStorageManager.isHoneypotBlock(block)) {
                if(Honeypot.getPlugin().getConfig().getBoolean("allow-explode")){
                    HoneypotBlockStorageManager.deleteBlock(block);
                } else {
                    foundHoneypotBlocks.add(block);
                }
            }
        }

        destroyedBlocks.removeAll(foundHoneypotBlocks);

    }

}
