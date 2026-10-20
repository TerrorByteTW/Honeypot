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

import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which chunks contain Honeypots, so chunks without any can skip the store entirely.
 * Reads are lock-free. Writers should hold this object's monitor across both the store write and the cache update so the two can't drift apart.
 */
@Singleton
public class HoneypotChunkCache {

    // World name -> (chunk key -> number of Honeypot regions overlapping that chunk)
    private final Map<String, Map<Long, Integer>> chunks = new ConcurrentHashMap<>();
    private volatile boolean hasUnloadedRegions = false;

    private static long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }

    public boolean contains(Block block) {
        Map<Long, Integer> world = chunks.get(block.getWorld().getName());
        return world != null && world.containsKey(chunkKey(block.getX() >> 4, block.getZ() >> 4));
    }

    public synchronized void add(HoneypotRegionObject region) {
        track(region, 1);
    }

    public synchronized void remove(HoneypotRegionObject region) {
        track(region, -1);
    }

    public synchronized void clear() {
        chunks.clear();
    }

    /**
     * Rebuilds the cache from the currently selected region store. Must be called whenever the region store is set or changed.
     */
    public synchronized void rebuild() {
        chunks.clear();
        boolean skipped = false;
        for (HoneypotRegionObject region : Registry.getRegionStore().getAllHoneypotRegions()) {
            skipped |= !track(region, 1);
        }
        hasUnloadedRegions = skipped;
    }

    /**
     * @return True if the last rebuild skipped regions because their world wasn't loaded
     */
    public boolean hasUnloadedRegions() {
        return hasUnloadedRegions;
    }

    // Returns false if the region's world isn't loaded, and it couldn't be tracked
    private boolean track(HoneypotRegionObject region, int delta) {
        Location p1 = region.getPos1(), p2 = region.getPos2();
        if (p1.getWorld() == null) return false;

        chunks.compute(p1.getWorld().getName(), (name, world) -> {
            if (world == null) world = new ConcurrentHashMap<>();
            for (int cx = Math.min(p1.getBlockX(), p2.getBlockX()) >> 4; cx <= Math.max(p1.getBlockX(), p2.getBlockX()) >> 4; cx++)
                for (int cz = Math.min(p1.getBlockZ(), p2.getBlockZ()) >> 4; cz <= Math.max(p1.getBlockZ(), p2.getBlockZ()) >> 4; cz++)
                    world.compute(chunkKey(cx, cz), (k, count) -> {
                        int n = (count == null ? 0 : count) + delta;
                        return n <= 0 ? null : n;
                    });
            return world.isEmpty() ? null : world;
        });
        return true;
    }
}
