package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.ConfigColorManager;
import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.api.events.HoneypotPlayerInteractEvent;
import me.terrorbyte.honeypot.api.events.HoneypotPrePlayerInteractEvent;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Objects;

public class PlayerBlockInteractListener implements Listener {

    /** 
     * Create a private constructor to hide the implicit one
     */
    PlayerBlockInteractListener() {

    }

    // Player block break event
    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings({"unchecked", "java:S3776"})
    public static void playerInteractEvent(PlayerInteractEvent event) {

        if (event.getPlayer().getTargetBlockExact(5) == null)
            return;
        if (!(event.getPlayer().getTargetBlockExact(5).getState() instanceof Container))
            return;
        if(!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;

        // We want to filter on inventories upon opening, not just creation (Like in the HoneypotCreate class) because
        // inventories can be both broken AND open :)
        if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories"))) {
            List<String> allowedBlocks = (List<String>) HoneypotConfigManager.getPluginConfig()
                    .getList("allowed-inventories");
            boolean allowed = false;

            for (String blockType : allowedBlocks) {
                if (Objects.requireNonNull(event.getPlayer().getTargetBlockExact(5)).getType().name()
                        .equals(blockType)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                return;
            }
        }

        try {
            if (!Objects.requireNonNull(event.getPlayer().getTargetBlockExact(5)).getType().equals(Material.ENDER_CHEST)
                    && Boolean.TRUE.equals(HoneypotBlockStorageManager
                            .isHoneypotBlock(Objects.requireNonNull(event.getPlayer().getTargetBlockExact(5))))) {
                // Fire HoneypotPrePlayerInteractEvent
                HoneypotPrePlayerInteractEvent hppie = new HoneypotPrePlayerInteractEvent(event.getPlayer(),
                        event.getClickedBlock());
                Bukkit.getPluginManager().callEvent(hppie);

                if (hppie.isCancelled())
                    return;

                if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-container-actions"))
                        && !(event.getPlayer().hasPermission("honeypot.exempt")
                                || event.getPlayer().hasPermission("honeypot.*") || event.getPlayer().isOp())) {
                    event.setCancelled(true);
                    openAction(event);
                }

                HoneypotPlayerInteractEvent hpie = new HoneypotPlayerInteractEvent(event.getPlayer(),
                        event.getClickedBlock());
                Bukkit.getPluginManager().callEvent(hpie);
            }
        }
        catch (NullPointerException npe) {
            // Do nothing as it's most likely an entity. If this event is triggered, the player will either be targeting
            // a block or entity, and there is no other option for it to be null.
        }
    }

    private static void openAction(PlayerInteractEvent event) {

        Block block = event.getPlayer().getTargetBlockExact(5);
        String chatPrefix = ConfigColorManager.getChatPrefix();
        Player player = event.getPlayer();

        assert block != null;
        String action = HoneypotBlockStorageManager.getAction(block);

        assert action != null;
        switch (action) {
        case "kick" -> player.kickPlayer(chatPrefix + " " + ConfigColorManager.getConfigMessage("kick"));

        case "ban" -> {
            String banReason = chatPrefix + " " + ConfigColorManager.getConfigMessage("ban");

            Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), banReason, null, chatPrefix);
            player.kickPlayer(banReason);
        }

        case "warn" -> event.getPlayer().sendMessage(chatPrefix + " " + ConfigColorManager.getConfigMessage("warn"));

        case "notify" -> {
            // Notify all staff members with permission or Op that someone tried to break a honeypot block
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("honeypot.notify") || p.hasPermission("honeypot.*") || p.isOp()) {
                    p.sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName()
                            + " was caught opening a Honeypot container at x=" + block.getX() + ", y=" + block.getY()
                            + ", z=" + block.getZ());
                }
            }

            Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(chatPrefix + " " + ChatColor.RED
                    + event.getPlayer().getName() + " was caught opening a Honeypot container");
        }

        default -> {
            if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-custom-actions"))) {
                String formattedAction = action.replace("%player%", event.getPlayer().getName());
                formattedAction = formattedAction.replace("%location%", event.getPlayer().getLocation().getX() + " "
                        + event.getPlayer().getLocation().getY() + " " + event.getPlayer().getLocation().getZ());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), formattedAction);
            }
        }
        }
    }
}
