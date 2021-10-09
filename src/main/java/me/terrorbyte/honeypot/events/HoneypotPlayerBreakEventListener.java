package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;

public class HoneypotPlayerBreakEventListener implements Listener {

    //Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void BlockBreakEvent(BlockBreakEvent event) throws IOException {
        if(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock())){

            boolean deleteBlock = false;

            if((Honeypot.getPlugin().getConfig().getBoolean("allow-player-destruction")) || (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())) {
                deleteBlock = true;
            } else {
                event.setCancelled(true);
            }

            if(Honeypot.getPlugin().getConfig().getInt("blocks-broken-before-action-taken") <= 1 || Honeypot.getPlugin().getConfig().getBoolean("allow-player-destruction")){
                breakAction(event);
            } else {
                countBreak(event);
            }

            if(deleteBlock) {
                HoneypotBlockStorageManager.deleteBlock(event.getBlock());
            }
        }
    }

    private static void breakAction(BlockBreakEvent event){
        Block block = event.getBlock();

        if(!(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())){

            String action = HoneypotBlockStorageManager.getAction(block);

            assert action != null;
            switch (action) {
                case "kick" ->
                        event.getPlayer().kickPlayer("[Honeypot] You have been kicked for breaking honeypot blocks");

                case "ban" -> {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), "[Honeypot] " + Honeypot.getPlugin().getConfig().getString("ban-reason"), null, "[Honeypot]");
                    event.getPlayer().kickPlayer("[Honeypot] " + Honeypot.getPlugin().getConfig().getString("ban-reason"));
                }

                case "warn" ->
                        event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + Honeypot.getPlugin().getConfig().getString("warn-message"));

                case "notify" -> {
                    //Notify all staff members with permission or Op that someone tried to break a honeypot block
                    for (Player player : Bukkit.getOnlinePlayers()){
                        if (player.hasPermission("honeypot.notify") || player.hasPermission("honeypot.*") || player.isOp()){
                            player.sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block at x=" + block.getX() + ", y=" + block.getY() + ", z=" + block.getZ());
                        }
                    }

                    Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block");
                }

                default -> {
                    //Do nothing
                }
            }
        } else if (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp()){
            HoneypotBlockStorageManager.deleteBlock(block);
            event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Just an FYI this was a honeypot. Since you broke it we've removed it");
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + "You are exempt from break actions, but do not have permissions to remove honeypot blocks. Sorry!");
        }
    }

    private static void countBreak(BlockBreakEvent event) throws IOException {
        int breaksBeforeAction = Honeypot.getPlugin().getConfig().getInt("blocks-broken-before-action-taken");
        int blocksBroken = HoneypotPlayerStorageManager.getCount(event.getPlayer().getName());
        blocksBroken += 1;

        if(blocksBroken == -1){
            HoneypotPlayerStorageManager.addPlayer(event.getPlayer().getName(), 1);
            blocksBroken = 1;
        }

        if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
            HoneypotPlayerStorageManager.setPlayerCount(event.getPlayer().getName(), 0);
            breakAction(event);
        } else {
            HoneypotPlayerStorageManager.setPlayerCount(event.getPlayer().getName(), blocksBroken);
        }
    }

}
