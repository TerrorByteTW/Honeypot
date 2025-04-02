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

package org.reprogle.honeypot.common.storagemanager;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;

public class HoneypotBlockManager {

    @Inject
    private HoneypotLogger logger;

    @Inject
    private CacheManager cacheManager;

    /**
     * Create a Honeypot {@link Block} and add it to the DB
     *
     * @param block  The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    public void createBlock(Block block, String action) {
        Registry.getStorageProvider().createHoneypotBlock(block, action);

        cacheManager.addToCache(new HoneypotBlockObject(block, action));
        logger.debug(Component.text("Created Honeypot block with action " + action + " at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
    }

    /**
     * Delete a block from the Honeypot DB
     *
     * @param block The Honeypot {@link Block} we're deleting
     */
    public void deleteBlock(Block block) {
        Registry.getStorageProvider().removeHoneypotBlock(block);

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

        if (Registry.getStorageProvider().isHoneypotBlock(block)) {
            String action = getAction(block);
            cacheManager.addToCache(new HoneypotBlockObject(block, action));
            return true;
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

        try {
            return Registry.getStorageProvider().getAction(block);
        } catch (Exception e) {
            logger.warning(Component.text("No action found for block, " +
                            "this is likely due to a container being created using the special lock format. " +
                            "This is only possible with commands, as Anvil's are not able to support strings that long." +
                            "Honeypot assumes these are Honeypots for performance. This can be safely ignored, but you need to investigate the block at: ")
                    .append((Component.text(block.getX() + ", " + block.getY() + ", " + block.getZ()))));
            return null;
        }
    }

    /**
     * Delete all Honeypots in the entire DB
     */
    public void deleteAllHoneypotBlocks(World world) {
        Registry.getStorageProvider().deleteAllHoneypotBlocks(world);
        logger.debug(Component.text("Deleted all Honeypot blocks!"));
    }

    /**
     * Get all {@link HoneypotBlockObject} in the DB
     *
     * @return An array list of all HoneypotBlockObjects
     */
    public List<HoneypotBlockObject> getAllHoneypots(World world) {
        return Registry.getStorageProvider().getAllHoneypots(world);
    }


}
