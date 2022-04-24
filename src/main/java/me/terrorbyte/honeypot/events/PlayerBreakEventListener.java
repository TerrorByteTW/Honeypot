package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import me.terrorbyte.honeypot.ConfigColorManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerBreakEventListener implements Listener {

    //Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void BlockBreakEvent(BlockBreakEvent event) throws IOException {
        if(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock())){

            boolean deleteBlock = false;

            if(Honeypot.config.getBoolean("allow-player-destruction") || (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())) {
                deleteBlock = true;
            } else {
                event.setCancelled(true);
            }

            if(Honeypot.config.getInt("blocks-broken-before-action-taken") <= 1 || Honeypot.config.getBoolean("allow-player-destruction")){
                breakAction(event);
            } else {
                if(!event.getPlayer().hasPermission("honeypot.exempt") && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("honeypot.remove")){
                    countBreak(event);
                } else {
                    breakAction(event);
                }
            }

            if(deleteBlock) {
                HoneypotBlockStorageManager.deleteBlock(event.getBlock());
            }
        }
    }

    private static void breakAction(BlockBreakEvent event){
        Block block = event.getBlock();
        String chatPrefix = ConfigColorManager.getChatPrefix();

        if(!(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())){

            String action = HoneypotBlockStorageManager.getAction(block);

            assert action != null;
            switch (action) {
                case "kick" ->
                        event.getPlayer().kickPlayer(chatPrefix + " " + ConfigColorManager.getConfigMessage("kick"));

                case "ban" -> {
                    String banReason = chatPrefix + " " + ConfigColorManager.getConfigMessage("ban");

                    Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), banReason, null, chatPrefix);
                    event.getPlayer().kickPlayer(banReason);
                }

                case "warn" ->
                        event.getPlayer().sendMessage(chatPrefix + " " + ConfigColorManager.getConfigMessage("warn"));

                case "notify" -> {
                    //Notify all staff members with permission or Op that someone tried to break a honeypot block
                    for (Player player : Bukkit.getOnlinePlayers()){
                        if (player.hasPermission("honeypot.notify") || player.hasPermission("honeypot.*") || player.isOp()){
                            player.sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block at x=" + block.getX() + ", y=" + block.getY() + ", z=" + block.getZ());
                        }
                    }

                    Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block");
                }

                default -> {
                    if(Honeypot.config.getBoolean("enable-custom-actions")){
                        String formattedAction = action.replace("%player%", event.getPlayer().getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), formattedAction);
                    }
                }
            }
        } else if (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp()){
            event.getPlayer().sendMessage(chatPrefix + " " + ChatColor.WHITE + "Just an FYI this was a honeypot. Since you broke it we've removed it");
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(chatPrefix + " " + ChatColor.RED + "You are exempt from break actions, but do not have permissions to remove honeypot blocks. Sorry!");
        }
    }

    private static void countBreak(BlockBreakEvent event) throws IOException {
        int breaksBeforeAction = Honeypot.config.getInt("blocks-broken-before-action-taken");
        int blocksBroken = HoneypotPlayerStorageManager.getCount(event.getPlayer());

        if(blocksBroken == -1){
            HoneypotPlayerStorageManager.addPlayer(event.getPlayer(), 0);
            blocksBroken = 0;
        }

        blocksBroken += 1;

        if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
            HoneypotPlayerStorageManager.setPlayerCount(event.getPlayer(), 0);
            breakAction(event);
        } else {
            HoneypotPlayerStorageManager.setPlayerCount(event.getPlayer(), blocksBroken);
        }
    }
}
