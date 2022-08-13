package org.reprogle.honeypot.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessEventListener implements Listener {
    
    /**
     * Create private constructor to hide the implicit one
     */
    PlayerCommandPreprocessEventListener() {

    }

    @EventHandler(priority = EventPriority.LOW)
    public static void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/hpteleport")) {
            event.setCancelled(true);
            String rawCommand = event.getMessage();
            String processedCommand = rawCommand.replace("/hpteleport", "minecraft:tp " + event.getPlayer().getName());

            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), processedCommand);
        }
    }

}
