/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.reprogle.honeypot.api.events.HoneypotInventoryClickEvent;
import org.reprogle.honeypot.api.events.HoneypotPreInventoryClickEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import com.samjakob.spigui.menu.SGMenu;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.*;

public class InventoryClickDragEventListener implements Listener {

    private final ActionHandler actionHandler;
    private final HoneypotBlockManager blockManager;
    private final HoneypotConfigManager configManager;
    private final HoneypotLogger logger;

    /**
     * Create package constructor to hide implicit one
     */
    @Inject
    InventoryClickDragEventListener(ActionHandler actionHandler, HoneypotBlockManager blockManager, HoneypotConfigManager configManager, HoneypotLogger logger) {
        this.actionHandler = actionHandler;
        this.blockManager = blockManager;
        this.configManager = configManager;
        this.logger = logger;
    }

    @SuppressWarnings({"java:S3776"})
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryClickEvent(InventoryClickEvent event) {
        // Sanity checks to ensure the clicker is a Player and the holder is a Container
        // that is NOT a custom one and is NOT their own inventory
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof DoubleChest || event.getInventory().getHolder() instanceof Container) || event.getInventory().getHolder() instanceof SGMenu)
            return;
        if (!EnumSet.of(SlotType.CONTAINER, SlotType.CRAFTING, SlotType.FUEL, SlotType.RESULT).contains(event.getSlotType())) return;
        if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

        final Block block = getBlock(event.getClickedInventory().getHolder());
        if (!blockManager.isHoneypotBlock(block)) return;

        final Inventory inventory = event.getInventory();

        if (!checkFilter(block)) return;

        if (!block.getType().equals(Material.ENDER_CHEST) && blockManager.isHoneypotBlock(Objects.requireNonNull(block))) {
            // Fire HoneypotPreInventoryClickEvent
            HoneypotPreInventoryClickEvent hpice = new HoneypotPreInventoryClickEvent(player, inventory);
            Bukkit.getPluginManager().callEvent(hpice);

            if (hpice.isCancelled()) return;

            if (!(player.hasPermission("honeypot.exempt") || player.hasPermission("honeypot.*") || player.isOp())) {

                // If the clicked slot is null, that means the slot didn't have something in it,
                // whether the player placed something in that slot. slot == null
                // corresponds to a click or place, not a take
                if (inventory.getItem(event.getSlot()) == null && configManager.getPluginConfig().getBoolean("container-actions.only-trigger-on-withdrawal")) {
                    return;
                }
                event.setCancelled(true);

                executeAction(player, block, inventory);
            }
        }
    }

    @SuppressWarnings({"java:S3776"})
    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventoryDragEvent(InventoryDragEvent event) {
        // Sanity checks to ensure the clicker is a Player and the holder is a Container
        // that is NOT a custom one and is NOT their own inventory
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Container) || event.getInventory().getHolder() instanceof SGMenu)
            return;
        if (event.getInventory().getType().equals(InventoryType.PLAYER)) return;

        final Block block = getBlock(event.getInventory().getHolder());
        final Inventory inventory = event.getInventory();

        if (!checkFilter(block)) return;

        if (!block.getType().equals(Material.ENDER_CHEST) && blockManager.isHoneypotBlock(Objects.requireNonNull(block))) {
            // Fire HoneypotPreInventoryClickEvent
            HoneypotPreInventoryClickEvent hpice = new HoneypotPreInventoryClickEvent(player, inventory);
            Bukkit.getPluginManager().callEvent(hpice);

            if (hpice.isCancelled()) return;

            if (!(player.hasPermission("honeypot.exempt") || player.hasPermission("honeypot.*") || player.isOp())) {

                event.setCancelled(true);

                executeAction(player, block, inventory);
            }
        }
    }

    private Block getBlock(InventoryHolder holder) {
        if (holder instanceof DoubleChest chest) {
            // Return the left block if it is a honeypot block
            // Otherwise always return the right side
            final Block left = ((Container) chest.getLeftSide()).getBlock();
            if (blockManager.isHoneypotBlock(left)) return left;
            return ((Container) chest.getRightSide()).getBlock();
        }
        return ((Container) holder).getBlock();
    }

    private void executeAction(Player player, Block block, Inventory inventory) {
        String action = blockManager.getAction(block);

        if (action == null) {
            logger.debug(Component.text("An InventoryClickEvent was called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + ". However, the action was null, so this must be a FAKE HONEYPOT. Please investigate the block at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            return;
        }

        logger.debug(Component.text("InventoryClickEvent being called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + ". Action is: " + action));

        actionHandler.handleCustomAction(action, block, player);

        HoneypotInventoryClickEvent hice = new HoneypotInventoryClickEvent(player, inventory);
        Bukkit.getPluginManager().callEvent(hice);

    }

    /**
     * Verifies a block against the inventory filter
     *
     * @param block The block to verify
     * @return True if filter is disabled or block is within it, otherwise false
     */
    @SuppressWarnings({"unchecked", "java:S3776"})
    private boolean checkFilter(Block block) {
        // We want to filter on inventories upon opening, not just creation (Like in the
        // HoneypotCreate class) because
        // inventories can be both broken AND open :)
        if (configManager.getPluginConfig().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) configManager.getPluginConfig().getList("allowed-inventories");

            for (String blockType : allowedBlocks) {
                if (Objects.requireNonNull(block).getType().name().equals(blockType)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

}
