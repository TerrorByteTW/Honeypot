package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.HoneypotManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class HoneypotBreakEventListener implements Listener {

    @EventHandler
    public static void BlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();

        if(!(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().isOp())){
            if(HoneypotManager.isHoneypotBlock(block)){
                event.setCancelled(true);
                String action = HoneypotManager.getAction(block);
                switch (action) {
                    case "kick" -> event.getPlayer().kickPlayer("[Honeypot] You have been kicked for breaking honeypot blocks");
                    case "ban" -> {
                        Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), "[Honeypot] You have been banned for breaking honeypot blocks", null, "[Honeypot]");
                        event.getPlayer().kickPlayer("[Honeypot] You have been banned for breaking honeypot blocks");
                    }
                    case "warn" -> event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + "Do not grief!");
                    case "notify" ->
                            //TODO - Add notification for all server staff
                            Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block");
                    default -> {
                    }
                }
            }
        }


    }
}
