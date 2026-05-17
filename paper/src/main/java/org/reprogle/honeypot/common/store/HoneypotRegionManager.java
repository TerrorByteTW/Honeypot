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

package org.reprogle.honeypot.common.store;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A class for managing Honeypot blocks. Interacts with the Registry to use the currently selected Store.
 */
public class HoneypotRegionManager {

    @Inject
    HoneypotLogger logger;

    /**
     * Create a Honeypot {@link Block} and add it to the DB
     *
     * @param block  The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    public void createBlock(Block block, String action) {
        Registry.getRegionStore().createHoneypotRegion(block, action);

        logger.debug(Component.text("Created Honeypot block with action " + action + " at " + block.getX() + ", " + block.getY() + ", " + block.getZ()), false);
    }

    /**
     * Creates a Honeypot region and adds it to the DB
     *
     * @param pos1   The first corner of the region
     * @param pos2   The second corner of the region
     * @param action The action of the Honeypot region
     */
    public void createRegion(Location pos1, Location pos2, String action) {
        Registry.getRegionStore().createHoneypotRegion(pos1, pos2, action);

        logger.debug(Component.text("Created Honeypot region with action " + action + " from " + pos1 + " to " + pos2), false);
    }

    /**
     * Delete a region from the Honeypot DB
     *
     * @param block The Honeypot {@link Block} we're deleting
     */
    public void deleteRegionContaining(Block block) {
        // Since the delete method may also be called on the wrong "half" of a block, we should use the same logical block resolver to obtain the "real" honeypot block
        Optional<Block> matched = resolveLogicalBlocks(block)
            .filter(b -> Registry.getRegionStore().isHoneypot(b.getLocation()))
            .findFirst();

        if (matched.isEmpty()) return;

        Registry.getRegionStore().removeHoneypotRegion(matched.get().getLocation());

        logger.debug(Component.text("Deleted Honeypot block at " + matched.get().getX() + ", " + matched.get().getY() + ", " + matched.get().getZ()), false);
    }

    /**
     * Check if the block is a Honeypot block
     *
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public boolean isHoneypotBlock(Block block) {
        Optional<Block> matched = resolveLogicalBlocks(block)
            .filter(b -> Registry.getRegionStore().isHoneypot(b.getLocation()))
            .findFirst();

        return matched.isPresent();
    }

    /**
     * Small helper function to determine if a block is a Double Chest, Bed, or Door (The most common things in MC that are "2-blocks big").
     * This omits uncommon blocks such as Pitcher Plants and Tall Grass, and will also not gracefully handle things like tall sugarcane or cactus.
     * This is a future improvement.
     *
     * @param block The block to resolve
     * @return A stream of one or more resolved blocks
     */
    private Stream<Block> resolveLogicalBlocks(Block block) {
        // Double chest
        if (block.getState() instanceof Chest chest
            && chest.getInventory().getHolder() instanceof DoubleChest dc) {
            return Stream.of(
                ((Chest) dc.getLeftSide()).getBlock(),
                ((Chest) dc.getRightSide()).getBlock()
            );
        }

        BlockData data = block.getBlockData();

        // Bed (head + foot)
        if (data instanceof Bed bed) {
            BlockFace facing = bed.getFacing();
            Block other = (bed.getPart() == Bed.Part.FOOT)
                ? block.getRelative(facing)
                : block.getRelative(facing.getOppositeFace());
            return Stream.of(block, other);
        }

        // Door (top + bottom)
        if (data instanceof Door door) {
            Block other = (door.getHalf() == Bisected.Half.BOTTOM)
                ? block.getRelative(BlockFace.UP)
                : block.getRelative(BlockFace.DOWN);
            return Stream.of(block, other);
        }

        // Default: just the block itself
        return Stream.of(block);
    }


    /**
     * Get the Honeypot Block object from Cache or the DB
     *
     * @param block The Block to retrieve as a Honeypot Block Object
     * @return The Honeypot Block Object if it exists, null if it doesn't
     */
    public HoneypotRegionObject getHoneypotRegion(Block block) {

        if (isHoneypotBlock(block))
            return new HoneypotRegionObject(block, getAction(block));

        return null;
    }

    /**
     * Return the action for the honeypot {@link Block}
     *
     * @param block The Block we're checking
     * @return The Honeypot's action as a string
     */
    public String getAction(Block block) {
        try {
            Optional<Block> matched = resolveLogicalBlocks(block)
                .filter(b -> Registry.getRegionStore().isHoneypot(b.getLocation()))
                .findFirst();
            if (matched.isEmpty()) throw new Exception("No action found");

            return Registry.getRegionStore().getAction(matched.get().getLocation());
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
    public void deleteAllHoneypotBlocks() {
        Registry.getRegionStore().deleteAllHoneypotRegions();

        logger.debug(Component.text("Deleted all Honeypot blocks!"), false);
    }

    /**
     * Get all {@link HoneypotRegionObject} in the DB
     *
     * @return An array list of all HoneypotRegionObjects
     */
    public List<HoneypotRegionObject> getAllHoneypots() {
        return Registry.getRegionStore().getAllHoneypotRegions();
    }

    /**
     * Gets all {@link HoneypotRegionObject} from the DB relative to the Location
     *
     * @param location The location to get nearby honeypots from
     * @param radius   The radius to search for nearby honeypots
     * @return A list of nearby HoneypotRegionObjects
     */
    public List<HoneypotRegionObject> getNearbyHoneypots(Location location, int radius) {
        return Registry.getRegionStore().getNearbyHoneypotRegions(location, radius);
    }

    /**
     * Checks if there is any overlap between two regions
     *
     * @param pos1 The first location to check for overlap
     * @param pos2 The second location to check for overlap
     * @return True if there is overlap, false otherwise
     */
    public boolean checkForOverlap(Location pos1, Location pos2) {
        return Registry.getRegionStore().checkForOverlap(pos1, pos2);
    }

}
