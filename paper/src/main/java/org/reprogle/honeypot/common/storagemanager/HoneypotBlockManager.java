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

package org.reprogle.honeypot.common.storagemanager;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.storagemanager.sqlite.SQLite;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public class HoneypotBlockManager {

    private final String storageMethod;
    private final boolean locking;

    @Inject
    private DataStoreManager dataStoreManager;

    @Inject
    private HoneypotLogger logger;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private SQLite db;

    public HoneypotBlockManager(String method, boolean locking) {
        if (method.equalsIgnoreCase("pdc")) {
            this.storageMethod = method;
        } else {
            this.storageMethod = "sqlite";
        }

        this.locking = locking;
    }

    /**
     * Create a Honeypot {@link Block} and add it to the DB
     *
     * @param block  The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    public void createBlock(Block block, String action) {
        if (block instanceof Container && locking) {
            ((Container) block).setLock(UUID.randomUUID().toString());
        }

        if (storageMethod.equals("pdc")) {
            dataStoreManager.createHoneypotBlock(block, action);
        } else {
            db.createHoneypotBlock(block, action);
        }

        cacheManager.addToCache(new HoneypotBlockObject(block, action));
        logger.debug(Component.text("Created Honeypot block with action " + action + " at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
    }

    /**
     * Delete a block from the Honeypot DB
     *
     * @param block The Honeypot {@link Block} we're deleting
     */
    public void deleteBlock(Block block) {
        // Remove lock if it's a container
        if (block instanceof Container) {
            ((Container) block).setLock(null);
        }

        if (storageMethod.equals("pdc")) {
            dataStoreManager.deleteBlock(block);
        } else {
            db.removeHoneypotBlock(block);
        }

        cacheManager.removeFromCache(new HoneypotBlockObject(block, null));
        logger.debug(Component.text("Deleted Honeypot block with at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
    }

    /**
     * Check if the block is a Honeypot block
     *
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public boolean isHoneypotBlock(Block block) {
        if (cacheManager.isInCache(new HoneypotBlockObject(block, null)) != null) {
            return true;
        }

        if (storageMethod.equals("pdc")) {
            if (dataStoreManager.isHoneypotBlock(block)) {
                String action = getAction(block);
                cacheManager.addToCache(new HoneypotBlockObject(block, action));
                if (block instanceof Container && locking) {
                    ((Container) block).setLock(UUID.randomUUID().toString());
                }
                return true;
            }
        } else {
            if (db.isHoneypotBlock(block)) {
                String action = getAction(block);
                cacheManager.addToCache(new HoneypotBlockObject(block, action));
                if (block instanceof Container && locking) {
                    ((Container) block).setLock(UUID.randomUUID().toString());
                }
                return true;
            }

        }
        return false;

    }

    /**
     * Get the Honeypot Block object from Cache or the DB
     *
     * @param block The Block to retrieve as a Honeypot Block Object
     * @return The Honeypot Block Object if it exists, null if it doesn't
     */
    public HoneypotBlockObject getHoneypotBlock(Block block) {

        if (isHoneypotBlock(block))
            return new HoneypotBlockObject(block, getAction(block));

        return null;
    }

    /**
     * Return the action for the honeypot {@link Block}
     *
     * @param block The Block we're checking
     * @return The Honeypot's action as a string
     */
    public String getAction(Block block) {
        // Check if block exists in cache. If it doesn't this will be null
        HoneypotBlockObject potential = cacheManager.isInCache(new HoneypotBlockObject(block, null));

        if (potential != null)
            return potential.getAction();

        if (storageMethod.equals("pdc")) {
            return dataStoreManager.getAction(block);

        } else {
            return db.getAction(block);
        }

    }

    /**
     * Delete all Honeypots in the entire DB
     */
    public void deleteAllHoneypotBlocks(@Nullable World world) {
        // Remove all locks from all container-based honeypots
        List<HoneypotBlockObject> blocks = this.getAllHoneypots(world);
        for (HoneypotBlockObject block : blocks) {
            if (block.getBlock() instanceof Container) {
                ((Container) block).setLock(null);
            }
        }

        if (storageMethod.equals("pdc")) {
            dataStoreManager.deleteAllHoneypotBlocks(world);
        } else {
            db.deleteAllBlocks();
        }

        logger.debug(Component.text("Deleted all Honeypot blocks!"));
    }

    /**
     * Get all {@link HoneypotBlockObject} in the DB
     *
     * @return An array list of all HoneypotBlockObjects
     */
    public List<HoneypotBlockObject> getAllHoneypots(@Nullable World world) {
        if (storageMethod.equals("pdc")) {
            return dataStoreManager.getAllHoneypots(world);
        } else {
            return db.getAllHoneypots();
        }
    }
}
