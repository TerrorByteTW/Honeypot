package me.terrorbyte.honeypot.events;

import java.io.IOException;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.terrorbyte.honeypot.ConfigColorManager;
import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.api.events.HoneypotPlayerBreakEvent;
import me.terrorbyte.honeypot.api.events.HoneypotPrePlayerBreakEvent;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;

public class PlayerBreakEventListener implements Listener {

    //Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void BlockBreakEvent(BlockBreakEvent event) throws IOException {
        if(HoneypotBlockStorageManager.isHoneypotBlock(event.getBlock())){
            // Fire HoneypotPrePlayerBreakEvent
            HoneypotPrePlayerBreakEvent hppbe = new HoneypotPrePlayerBreakEvent(event.getPlayer(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hppbe);

            // Create a boolean for if we should remove the block from the DB or not
            boolean deleteBlock = false;

            // If Allow Player Destruction is true, the player has permissions or is Op, flag the block for deletion from the DB. Otherwise, set the BlockBreakEvent to cancelled
            if(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction") || (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())) {
                deleteBlock = true;
            } else {
                event.setCancelled(true);
            }

            // If blocks broken before action is less than or equal to 1, or allow destruction is enabled, just go to the break action. Otherwise, count it
            if(HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken") <= 1 || HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction")){
                breakAction(event);
            } else {
                // If the player is not exempt, not op, and do not have remove perms, count the break. Otherwise, just activate the break action.
                if(!event.getPlayer().hasPermission("honeypot.exempt") && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("honeypot.remove")){
                    countBreak(event);
                } else {
                    breakAction(event);
                }
            }

            // Fire HoneypotPlayerBreakEvent
            HoneypotPlayerBreakEvent hpbe = new HoneypotPlayerBreakEvent(event.getPlayer(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hpbe);

            // If we flagged the block for deletion, remove it from the DB. Do this after other actions have been completed, otherwise the other actions will fail with NPEs
            if(deleteBlock) {
                HoneypotBlockStorageManager.deleteBlock(event.getBlock());
            }
        }
    }

    private static void breakAction(BlockBreakEvent event){
        // Get the block broken and the chat prefix for prettiness
        Block block = event.getBlock();
        String chatPrefix = ConfigColorManager.getChatPrefix();

        // If the player isn't exempt, doesn't have permissions, or isn't Op
        if(!(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())){

            // Grab the action from the block via the storage manager
            String action = HoneypotBlockStorageManager.getAction(block);

            // Run certain actions based on the action of the Honeypot Block
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
                            player.sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block at x=" + block.getX() + ", y=" + block.getY() + ", z=" + block.getZ() + " in world " + block.getWorld().getName());
                        }
                    }

                    Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName() + " was caught breaking a Honeypot block");
                }

                case "nothing" -> {
                    //Do...nothing
                }

                default -> {
                    // Default path is likely due to custom actions. Check if custom actions are enabled, then run whatever the action was as the server
                    if(HoneypotConfigManager.getPluginConfig().getBoolean("enable-custom-actions")){
                        String formattedAction = action.replace("%player%", event.getPlayer().getName());
                        formattedAction = formattedAction.replace("%location%", event.getPlayer().getLocation().getX() + " " + event.getPlayer().getLocation().getY() + " " + event.getPlayer().getLocation().getZ());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), formattedAction);
                    }
                }
            }

        // At this point we know the player has one of those permissions above. Now we need to figure out which 
        } else if (event.getPlayer().hasPermission("honeypot.remove") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp()){
            event.getPlayer().sendMessage(chatPrefix + " " + ChatColor.WHITE + "Just an FYI this was a honeypot. Since you broke it we've removed it");

        // If it got to here, then they are exempt but can't break blocks anyway.
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(chatPrefix + " " + ChatColor.RED + "You are exempt from break actions, but do not have permissions to remove honeypot blocks. Sorry!");
        }
    }

    private static void countBreak(BlockBreakEvent event) throws IOException {

        // Get the config value and the amount of blocks broken
        int breaksBeforeAction = HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken");
        int blocksBroken = HoneypotPlayerStorageManager.getCount(event.getPlayer());

        // getCount returns -1 if the player doesn't exist in the DB. If that's the case, add the player to the DB
        if(blocksBroken == -1){
            HoneypotPlayerStorageManager.addPlayer(event.getPlayer(), 0);
            blocksBroken = 0;
        }

        // Increment the blocks broken counter
        blocksBroken += 1;

        // If the blocks broken are larger than the breaks before action or if breaks before action equal equals 1, reset the count and perform the break
        if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
            HoneypotPlayerStorageManager.setPlayerCount(event.getPlayer(), 0);
            breakAction(event);
        } else {
            // Just count it
            HoneypotPlayerStorageManager.setPlayerCount(event.getPlayer(), blocksBroken);
        }
    }
}
