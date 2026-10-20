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

package org.reprogle.honeypot.common.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * A class for managing Honeypot blocks. Interacts with the Registry to use the currently selected Store.
 */
@Singleton
public class HoneypotRegionManager {

    @Inject
    HoneypotLogger logger;

    /**
     * World name -> (chunk key -> number of Honeypot regions overlapping that chunk).
     * Used as a fast negative check so chunks without Honeypots never hit the store.
     * Keyed by world name to match how stores persist worlds (and because worlds may not be loaded when the cache is built).
     */
    private volatile Map<String, Map<Long, AtomicInteger>> honeypotChunks = new ConcurrentHashMap<>();

    private static long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }

    private static void track(Map<String, Map<Long, AtomicInteger>> cache, String world, int x1, int z1, int x2, int z2, int delta) {
        Map<Long, AtomicInteger> chunks = cache.computeIfAbsent(world, k -> new ConcurrentHashMap<>());
        int minCx = Math.min(x1, x2) >> 4, maxCx = Math.max(x1, x2) >> 4;
        int minCz = Math.min(z1, z2) >> 4, maxCz = Math.max(z1, z2) >> 4;

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                long key = chunkKey(cx, cz);
                if (delta > 0) {
                    chunks.computeIfAbsent(key, k -> new AtomicInteger()).addAndGet(delta);
                } else {
                    chunks.computeIfPresent(key, (k, count) -> count.addAndGet(delta) <= 0 ? null : count);
                }
            }
        }
    }

    private void track(HoneypotRegionObject region, int delta) {
        track(honeypotChunks, region.getWorldName(),
            region.getPos1().getBlockX(), region.getPos1().getBlockZ(),
            region.getPos2().getBlockX(), region.getPos2().getBlockZ(), delta);
    }

    private boolean chunkHasHoneypot(Block block) {
        Map<Long, AtomicInteger> chunks = honeypotChunks.get(block.getWorld().getName());
        return chunks != null && chunks.containsKey(chunkKey(block.getX() >> 4, block.getZ() >> 4));
    }

    private static boolean isOnChunkEdge(Block block) {
        int x = block.getX() & 15, z = block.getZ() & 15;
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    /**
     * Rebuilds the chunk cache from the currently selected region store.
     * Must be called whenever the region store is set or changed.
     */
    public void rebuildChunkCache() {
        Map<String, Map<Long, AtomicInteger>> fresh = new ConcurrentHashMap<>();
        List<HoneypotRegionObject> regions = Registry.getRegionStore().getAllHoneypotRegions();

        for (HoneypotRegionObject region : regions) {
            track(fresh, region.getWorldName(),
                region.getPos1().getBlockX(), region.getPos1().getBlockZ(),
                region.getPos2().getBlockX(), region.getPos2().getBlockZ(), 1);
        }

        honeypotChunks = fresh;
        logger.debug(Component.text("Rebuilt Honeypot chunk cache with " + regions.size() + " regions"));
    }

    /**
     * Create a Honeypot {@link Block} and add it to the DB
     *
     * @param block  The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    public void createBlock(Block block, String action) {
        Registry.getRegionStore().createHoneypotRegion(block, action);

        track(new HoneypotRegionObject(block, action), 1);

        logger.debug(Component.text("Created Honeypot block with action " + action + " at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
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

        track(new HoneypotRegionObject(pos1, pos2, action), 1);

        logger.debug(Component.text("Created Honeypot region with action " + action + " from " + pos1 + " to " + pos2));
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

        // Grab the full region before removing it, since the store deletes the entire region containing this location
        HoneypotRegionObject region = Registry.getRegionStore().getHoneypotRegion(matched.get().getLocation());

        Registry.getRegionStore().removeHoneypotRegion(matched.get().getLocation());

        if (region != null) track(region, -1);

        logger.debug(Component.text("Deleted Honeypot block at " + matched.get().getX() + ", " + matched.get().getY() + ", " + matched.get().getZ()));
    }

    /**
     * Check if the block is a Honeypot block
     *
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public boolean isHoneypotBlock(Block block) {
        // Fast path: skip the store entirely if the block isn't in a chunk containing a Honeypot. Two-block structures
        // (chests, beds) can straddle a chunk border, so blocks on a chunk edge also check their other half's chunk
        if (!chunkHasHoneypot(block)
            && (!isOnChunkEdge(block) || resolveLogicalBlocks(block).noneMatch(this::chunkHasHoneypot))) return false;

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
     * Get the Honeypot Region Object
     *
     * @param block The Block to retrieve as a Honeypot Region Object
     * @return The Honeypot Region Object if it exists, null if it doesn't
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
        honeypotChunks = new ConcurrentHashMap<>();

        logger.debug(Component.text("Deleted all Honeypot blocks!"));
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
