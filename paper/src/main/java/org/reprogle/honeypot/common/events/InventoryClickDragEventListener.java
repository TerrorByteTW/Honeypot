/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022-2026 TerrorByte and Honeypot Contributors.
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

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
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
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.*;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.ActionHandler;

import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class InventoryClickDragEventListener implements Listener, IHoneypotEvent {

    private final ActionHandler actionHandler;
    private final HoneypotRegionManager regionManager;
    private final BytePluginConfig config;
    private final HoneypotLogger logger;

    @Inject
    InventoryClickDragEventListener(ActionHandler actionHandler, HoneypotRegionManager regionManager, BytePluginConfig config, HoneypotLogger logger) {
        this.actionHandler = actionHandler;
        this.regionManager = regionManager;
        this.config = config;
        this.logger = logger;
    }

    @SuppressWarnings({"java:S3776"})
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (!useInventoryClick()) return;

        // Sanity checks to ensure the clicker is a Player and the holder is a Container
        // that is NOT a custom one and is NOT their own inventory
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Because for some reason DoubleChest is its own class and does not implement Container. It only *extends* InventoryHolder :|
        if (!(event.getInventory().getHolder() instanceof DoubleChest || event.getInventory().getHolder() instanceof Container) || event.getInventory().getHolder() instanceof ChestGui)
            return;

        // Support weird slot types that may bypass the checks
        if (!EnumSet.of(SlotType.CONTAINER, SlotType.CRAFTING, SlotType.FUEL, SlotType.RESULT).contains(event.getSlotType()))
            return;

        //noinspection DataFlowIssue getType() is marked @NotNull but IntelliJ thinks otherwise
        if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

        InventoryHolder holder = event.getInventory().getHolder();

        // Stupid hack because DoubleChest is the ONLY inventory in the entire game that implements InventoryHolder instead of extending Container.
        Block block;
        if (holder instanceof DoubleChest doubleChest) {
            //noinspection DataFlowIssue Same issue as above, getBlockAt() is claiming to be nullable when it clearly is marked @NotNull
            block = doubleChest.getWorld().getBlockAt(doubleChest.getLocation());
        } else {
            //noinspection DataFlowIssue
            block = ((Container) event.getClickedInventory().getHolder()).getBlock();
        }

        if (!regionManager.isHoneypotBlock(block)) return;

        final Inventory inventory = event.getInventory();

        if (!checkFilter(block)) return;

        if (!block.getType().equals(Material.ENDER_CHEST) && regionManager.isHoneypotBlock(Objects.requireNonNull(block))) {
            logger.verbose(Component.text("InventoryClickEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));

            // Fire HoneypotPreInventoryClickEvent
            var hpice = new HoneypotPreInventoryClickEvent(player, inventory);
            var hpte = new HoneypotPreTriggerEvent(player, block, TriggerType.INVENTORY_INTERACT);

            // Both events are always fired, and cancelling either one stops processing
            boolean preClick = hpice.callEvent();
            boolean preTrigger = hpte.callEvent();
            if (!preClick || !preTrigger) return;

            if (!actionHandler.isExempt(player, TriggerType.INVENTORY_INTERACT)) {

                // If the clicked slot is null, that means the slot didn't have something in it,
                // whether the player placed something in that slot. slot == null
                // corresponds to a click or place, not a take
                if (inventory.getItem(event.getSlot()) == null && config.config().getBoolean("container-actions.only-trigger-on-withdrawal")) {
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
        if (!useInventoryClick()) return;

        // Sanity checks to ensure the clicker is a Player and the holder is a Container
        // that is NOT a custom one and is NOT their own inventory
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // Because for some reason DoubleChest is its own class and does not implement Container. It only implements InventoryHolder :|
        if (!(event.getInventory().getHolder() instanceof DoubleChest || event.getInventory().getHolder() instanceof Container) || event.getInventory().getHolder() instanceof ChestGui)
            return;
        if (event.getInventory().getType().equals(InventoryType.PLAYER)) return;

        InventoryHolder holder = event.getInventory().getHolder();

        Block block;
        // Stupid hack because DoubleChest is the ONLY inventory in the entire game that implements InventoryHolder instead of extending Container.
        if (holder instanceof DoubleChest doubleChest) {
            //noinspection DataFlowIssue Same issue as above, getBlockAt() is claiming to be nullable when it clearly is marked @NotNull
            block = doubleChest.getWorld().getBlockAt(doubleChest.getLocation());
        } else {
            //noinspection DataFlowIssue
            block = ((Container) event.getInventory().getHolder()).getBlock();
        }

        final Inventory inventory = event.getInventory();

        if (!checkFilter(block)) return;

        if (!block.getType().equals(Material.ENDER_CHEST) && regionManager.isHoneypotBlock(Objects.requireNonNull(block))) {
            logger.verbose(Component.text("InventoryClickEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            // Fire HoneypotPreInventoryClickEvent
            var hpice = new HoneypotPreInventoryClickEvent(player, inventory);
            var hpte = new HoneypotPreTriggerEvent(player, block, TriggerType.INVENTORY_INTERACT);

            // Both events are always fired, and cancelling either one stops processing
            boolean preClick = hpice.callEvent();
            boolean preTrigger = hpte.callEvent();
            if (!preClick || !preTrigger) return;

            if (!actionHandler.isExempt(player, TriggerType.INVENTORY_INTERACT)) {

                event.setCancelled(true);

                executeAction(player, block, inventory);
            }
        }
    }

    /**
     * Container actions on click only apply if they're enabled and inventory clicks are being used instead of opening
     *
     * @return True if inventory clicks and drags should be processed
     */
    private boolean useInventoryClick() {
        return config.config().getBoolean("container-actions.enable-container-actions")
            && config.config().getBoolean("container-actions.use-inventory-click");
    }

    private void executeAction(Player player, Block block, Inventory inventory) {
        actionHandler.checkAndHandle(player, block, TriggerType.INVENTORY_INTERACT);

        new HoneypotInventoryClickEvent(player, inventory).callEvent();
        new HoneypotTriggerEvent(player, block, TriggerType.INVENTORY_INTERACT).callEvent();
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
        if (config.config().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) config.config().getList("allowed-inventories");

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
