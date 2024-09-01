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

package org.reprogle.honeypot.common.utils;

import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HoneypotInventory implements InventoryHolder {
    private final Container container;
    private final Inventory dummyInventory;

    /**
     * Create a new Honeypot Inventory
     * @param container The container that will "own" this inventory
     */
    public HoneypotInventory(Container container) {
        this.container = container;
        this.dummyInventory = container.getInventory();
    }

    /**
     * Get the inventory of the block
     * @return The inventory of the block
     */
    @Override
    public @NotNull Inventory getInventory() {
        return dummyInventory;
    }

    /**
     * Updates the inventory, optionally forcefully
     * @param force True to update the inventory by force
     * @return True if the update was successful, false if not
     */
    public boolean updateInventory(boolean force, ItemStack[] itemStacks) {
        container.getInventory().setContents(itemStacks);
        return container.update(force);
    }

    /**
     * Updates the inventory forcefully
     *
     * @return True if the inventory was updated, false if not
     */
    public boolean updateInventory(ItemStack[] itemStacks) {
        return updateInventory(true, itemStacks);
    }
}
