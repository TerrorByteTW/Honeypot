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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.*;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.ActionHandler;

import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.Arrays;
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

        InventoryHolder holder = event.getInventory().getHolder();

        // Because for some reason DoubleChest is its own class and does not implement Container. It only *extends* InventoryHolder :|
        if (!(holder instanceof DoubleChest || holder instanceof Container) || holder instanceof ChestGui)
            return;

        final Inventory inventory = event.getInventory();
        final Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        Interaction interaction = classify(event, inventory, clicked.equals(inventory));
        if (interaction == Interaction.NONE) return;
        if (interaction == Interaction.DEPOSIT && onlyTriggerOnWithdrawal()) return;

        process(player, inventory, event);
    }

    @SuppressWarnings({"java:S3776"})
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryDragEvent(InventoryDragEvent event) {
        if (!useInventoryClick()) return;

        // Sanity checks to ensure the clicker is a Player and the holder is a Container
        // that is NOT a custom one and is NOT their own inventory
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();

        // Because for some reason DoubleChest is its own class and does not implement Container. It only implements InventoryHolder :|
        if (!(holder instanceof DoubleChest || holder instanceof Container) || holder instanceof ChestGui)
            return;
        final Inventory inventory = event.getInventory();

        // Drags only ever place items, so they're always deposits, and only matter if they touch the top inventory
        if (onlyTriggerOnWithdrawal()) return;
        if (event.getRawSlots().stream().noneMatch(slot -> slot < inventory.getSize())) return;

        process(player, inventory, event);
    }

    private void process(Player player, Inventory inventory, InventoryInteractEvent event) {
        Block block = resolveBlock(inventory.getHolder());

        if (!checkFilter(block)) return;

        if (!block.getType().equals(Material.ENDER_CHEST) && regionManager.isHoneypotBlock(block)) {
            logger.verbose(Component.text((event instanceof InventoryClickEvent ? "InventoryClickEvent" : "InventoryDragEvent") + " being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
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

    private boolean onlyTriggerOnWithdrawal() {
        return config.config().getBoolean("container-actions.only-trigger-on-withdrawal");
    }

    private enum Interaction {NONE, DEPOSIT, WITHDRAWAL}

    /**
     * Determines how a click affects the Honeypot (top) inventory
     *
     * @param event      The click event
     * @param top        The Honeypot inventory
     * @param clickedTop Whether the clicked slot belongs to the Honeypot inventory
     * @return The kind of interaction this click has with the Honeypot inventory
     */
    private Interaction classify(InventoryClickEvent event, Inventory top, boolean clickedTop) {
        InventoryAction action = event.getAction();

        // Double-clicking gathers matching items from both inventories, regardless of which side was clicked
        if (action == InventoryAction.COLLECT_TO_CURSOR) {
            ItemStack cursor = event.getCursor();
            return Arrays.stream(top.getContents()).anyMatch(cursor::isSimilar) ? Interaction.WITHDRAWAL : Interaction.NONE;
        }

        if (!clickedTop) {
            // Shift-clicking from the player inventory is the only other way to affect the top inventory
            return action == InventoryAction.MOVE_TO_OTHER_INVENTORY ? Interaction.DEPOSIT : Interaction.NONE;
        }

        return switch (action) {
            case PLACE_ALL, PLACE_SOME, PLACE_ONE, PLACE_FROM_BUNDLE, PLACE_ALL_INTO_BUNDLE, PLACE_SOME_INTO_BUNDLE ->
                Interaction.DEPOSIT;
            case NOTHING -> Interaction.NONE;
            // Everything else (pickups, swaps, drops, shift-clicks out, etc.) removes the item in the clicked slot, if there is one
            default -> event.getCurrentItem() == null || event.getCurrentItem().isEmpty()
                ? Interaction.DEPOSIT
                : Interaction.WITHDRAWAL;
        };
    }

    /**
     * Resolves the block backing a container inventory.
     * DoubleChest is the only holder that doesn't implement Container, so it needs special handling.
     */
    private Block resolveBlock(InventoryHolder holder) {
        if (holder instanceof DoubleChest doubleChest) {
            //noinspection DataFlowIssue getBlockAt() is marked @NotNull
            return doubleChest.getWorld().getBlockAt(doubleChest.getLocation());
        }
        return ((Container) holder).getBlock();
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
