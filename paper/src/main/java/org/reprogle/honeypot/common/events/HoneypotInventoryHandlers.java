/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotInventory;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.HashMap;
import java.util.UUID;

public class HoneypotInventoryHandlers implements Listener {

    private final HoneypotBlockManager blockManager;
    private final HoneypotLogger logger;
    private final Honeypot plugin;

    private final HashMap<UUID, Block> openInventories = new HashMap<>();

    /**
     * Create package constructor to hide implicit one
     */
    @Inject
    HoneypotInventoryHandlers(HoneypotBlockManager blockManager, HoneypotLogger logger, Honeypot plugin) {
        this.blockManager = blockManager;
        this.logger = logger;
        this.plugin = plugin;
    }

    /**
     * Small utility method to ensure locked Honeypots can still be opened
     *
     * @param event Event that was triggered
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryInteractEvent(PlayerInteractEvent event) {
        Block block = event.getPlayer().getTargetBlockExact(5);

        if (block == null) return;
        if (event.getPlayer().isSneaking()) return;
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;
        if (!(block.getState() instanceof Container blockState)) return;
        if (!blockManager.isHoneypotBlock(block)) return;

        event.setCancelled(true);

        // Force the inventory to open if it's locked anyway, since the only purpose of the lock is to prevent hoppers and droppers from interacting with it
        if (blockState.isLocked()) {
            InventoryType inventoryType = blockState.getInventory().getType();

            // Get the original title of the inventory. If the custom name is null, set the name
            Component title = blockState.customName() != null ? blockState.customName() : Component.text(blockState.getType().toString().substring(0, 1).toUpperCase() + blockState.getType().toString().substring(1).toLowerCase());

            HoneypotInventory inventory = new HoneypotInventory(blockState.getInventory());

            Inventory dummyInventory = Bukkit.createInventory(inventory, inventoryType, title);
            dummyInventory.setContents(blockState.getSnapshotInventory().getContents());

            openInventories.put(event.getPlayer().getUniqueId(), block);

            event.getPlayer().openInventory(dummyInventory);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof HoneypotInventory) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Get the block that the player attempted to open. This is added to the HashMap above.
                    Block block = openInventories.get(event.getPlayer().getUniqueId());
                    Container container = (Container) block.getState();
                    openInventories.remove(event.getPlayer().getUniqueId());

                    // Set the contents of the original inventory to the contents of the HoneypotInventory
                    container.getSnapshotInventory().setContents(event.getInventory().getContents());
                    boolean success = container.update(true, true);

                    logger.info(Component.text(success));

                    Location location = container.getBlock().getLocation();
                    // Manually get the block so we know it's the one in the world and not the one we have saved
                    Block block2 = event.getPlayer().getWorld().getBlockAt(location);
                    for (ItemStack stack : ((Container) block2.getState()).getInventory().getContents()) {
                        if (stack == null) continue;
                        logger.info(Component.text(stack.toString()));
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
