package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotConfigColorManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Objects;

public class HoneypotPlayerContainerOpenListener implements Listener {

    //Player block break event
    @EventHandler(priority = EventPriority.LOW)
    public static void InventoryOpenEvent(InventoryOpenEvent event) {
        if (!(Objects.requireNonNull(event.getPlayer().getTargetBlockExact(10)).getType().equals(Material.ENDER_CHEST)) && HoneypotBlockStorageManager.isHoneypotBlock(Objects.requireNonNull(event.getPlayer().getTargetBlockExact(10)))) {
            if(Honeypot.getPlugin().getConfig().getBoolean("enable-container-actions") && !(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())){
                event.setCancelled(true);
                openAction(event);
            }
        }
    }

    private static void openAction(InventoryOpenEvent event){
        Block block = event.getPlayer().getTargetBlockExact(10);
        String chatPrefix = HoneypotConfigColorManager.getChatPrefix();
        Player player = Bukkit.getPlayer(event.getPlayer().getName());

        if(!(event.getPlayer().hasPermission("honeypot.exempt") || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())){

            assert block != null;
            String action = HoneypotBlockStorageManager.getAction(block);

            assert action != null;
            assert player != null;
            switch (action) {
                case "kick" -> player.kickPlayer(chatPrefix + " " + HoneypotConfigColorManager.getConfigMessage("kick"));

                case "ban" -> {
                    String banReason = chatPrefix + " " + HoneypotConfigColorManager.getConfigMessage("ban");

                    Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), banReason, null, chatPrefix);
                    player.kickPlayer(banReason);
                }

                case "warn" ->
                        event.getPlayer().sendMessage(chatPrefix + " " + HoneypotConfigColorManager.getConfigMessage("warn"));

                case "notify" -> {
                    //Notify all staff members with permission or Op that someone tried to break a honeypot block
                    for (Player p : Bukkit.getOnlinePlayers()){
                        if (p.hasPermission("honeypot.notify") || p.hasPermission("honeypot.*") || p.isOp()){
                            p.sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName() + " was caught opening a Honeypot container at x=" + block.getX() + ", y=" + block.getY() + ", z=" + block.getZ());
                        }
                    }

                    Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName() + " was caught opening a Honeypot container");
                }

                default -> {
                    //Do nothing
                }
            }
        }
    }
}
