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

        if(!(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())){
            if(HoneypotManager.isHoneypotBlock(block)){
                event.setCancelled(true);
                String action = HoneypotManager.getAction(block);

                assert action != null;
                switch (action) {
                    case "kick" -> event.getPlayer().kickPlayer("[Honeypot] You have been kicked for breaking honeypot blocks");
                    case "ban" -> {
                        Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), "[Honeypot] You have been banned for breaking honeypot blocks", null, "[Honeypot]");
                        event.getPlayer().kickPlayer("[Honeypot] You have been banned for breaking honeypot blocks");
                    }
                    case "warn" -> event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + "Do not grief!");
                    case "notify" -> {
                        //Notify all staff members with permission or Op that someone tried to break a honeypot block
                        for (Player player : Bukkit.getOnlinePlayers()){
                            if (player.hasPermission("honeypot.notify") || player.hasPermission("honeypot.*") || player.isOp()){
                                Bukkit.broadcast(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block at x=" + block.getX() + ", y=" + block.getY() + ", z=" + block.getZ(),"honeypot.notify");
                            }
                        }

                        Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block");
                    }
                    default -> {
                        //Do nothing
                    }
                }
            }
        } else if (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp()){
            HoneypotManager.deleteBlock(block);
            event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Just an FYI this was a honeypot. Since you broke it we've removed it");
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + "You are exempt from break actions, but do not have permissions to remove them");
        }


    }
}
