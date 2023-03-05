package org.reprogle.honeypot.events;

import java.util.List;
import java.util.Objects;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotInventoryClickEvent;
import org.reprogle.honeypot.api.events.HoneypotPreInventoryClickEvent;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.utils.ActionHandler;
import org.reprogle.honeypot.utils.HoneypotConfigManager;

public class InventoryClickEventListener implements Listener {

    /**
     * Create package constructor to hide implicit one
     */
    InventoryClickEventListener() {
    }

    @SuppressWarnings({ "unchecked", "java:S3776" })
    @EventHandler(priority = EventPriority.HIGH)
    public static void inventoryClickEvent(InventoryClickEvent event) {
        // Sanity checks to ensure the clicker is a Player and the holder is a Container
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (!(event.getInventory().getHolder() instanceof Container))
            return;
        if (event.getSlotType() != SlotType.CONTAINER)
            return;

        final Block block = ((Container) event.getInventory().getHolder()).getBlock();
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();

        // We want to filter on inventories upon opening, not just creation (Like in the
        // HoneypotCreate class) because
        // inventories can be both broken AND open :)
        if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories"))) {
            List<String> allowedBlocks = (List<String>) HoneypotConfigManager.getPluginConfig()
                    .getList("allowed-inventories");
            boolean allowed = false;

            for (String blockType : allowedBlocks) {
                if (Objects.requireNonNull(block).getType().name().equals(blockType)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                return;
            }
        }

        if (!block.getType().equals(Material.ENDER_CHEST)
                && Boolean.TRUE.equals(HoneypotBlockManager.getInstance()
                        .isHoneypotBlock(Objects.requireNonNull(block)))) {
            // Fire HoneypotPreInventoryClickEvent
            HoneypotPreInventoryClickEvent hpice = new HoneypotPreInventoryClickEvent(player, inventory);
            Bukkit.getPluginManager().callEvent(hpice);

            if (hpice.isCancelled())
                return;

            if (!(player.hasPermission("honeypot.exempt")
                    || player.hasPermission("honeypot.*") || player.isOp())) {
                if (event.getInventory().getItem(event.getSlot()) == null && HoneypotConfigManager.getPluginConfig()
                        .getBoolean("container-actions.only-trigger-on-withdrawal")) {
                    return;
                } else {
                    event.setCancelled(true);
                    executeAction(event);
                }
            }

            HoneypotInventoryClickEvent hice = new HoneypotInventoryClickEvent(player,
                    inventory);
            Bukkit.getPluginManager().callEvent(hice);
        }
    }

    private static void executeAction(InventoryClickEvent event) {

        final Block block = ((Container) event.getInventory().getHolder()).getBlock();
        final Player player = (Player) event.getWhoClicked();

        assert block != null;
        String action = HoneypotBlockManager.getInstance().getAction(block);

        assert action != null;
        Honeypot.getHoneypotLogger().log("InventoryClickEvent being called for player: " + player.getName()
                + ", UUID of " + player.getUniqueId() + ". Action is: " + action);

        switch (action) {
            case "kick" -> player.kickPlayer(CommandFeedback.sendCommandFeedback("kick"));

            case "ban" -> {
                String banReason = CommandFeedback.sendCommandFeedback("ban");

                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), banReason, null,
                        CommandFeedback.getChatPrefix());
                player.kickPlayer(banReason);
            }

            case "warn" ->
                player.sendMessage(CommandFeedback.sendCommandFeedback("warn"));

            case "notify" -> {
                // Notify all staff members with permission or Op that someone tried to break a
                // honeypot block
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("honeypot.notify") || p.hasPermission("honeypot.*") || p.isOp()) {
                        p.sendMessage(
                                CommandFeedback.getChatPrefix() + " " + ChatColor.RED + player.getName()
                                        + " was caught opening a Honeypot container at x=" + block.getX() + ", y="
                                        + block.getY()
                                        + ", z=" + block.getZ());
                    }
                }

                Honeypot.getPlugin().getServer().getConsoleSender()
                        .sendMessage(CommandFeedback.getChatPrefix() + " " + ChatColor.RED
                                + player.getName() + " was caught opening a Honeypot container");
            }

            default -> {
                ActionHandler.handleCustomAction(action, block, player);
            }
        }
    }

}
