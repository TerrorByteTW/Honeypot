/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
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

package org.reprogle.honeypot.storagemanager;

import org.reprogle.honeypot.utils.HoneypotConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A rudimentary caching utility to lessen dependency on databases used by Honeypot
 */
public class CacheManager {

    private static final List<HoneypotBlockObject> cache = new ArrayList<>();

    // Create constructor to hide implicit one
    CacheManager() {

    }

    /**
     * Gets the entire cache list for further processing if necessary
     *
     * @return The List of {@link HoneypotBlockObject} in the cache
     */
    public static List<HoneypotBlockObject> getCache() {
        return cache;
    }

    /**
     * Adds a {@link HoneypotBlockObject} to the cache
     *
     * @param block The honeypot to add to cache
     */
    public static void addToCache(HoneypotBlockObject block) {
        int cacheSize = HoneypotConfigManager.getPluginConfig().getInt("cache-size");

        if (cacheSize <= 0) return;

        if (cache.size() <= cacheSize && cache.size() <= 50) {
            cache.add(block);
        } else {
            cache.remove(0);
            cache.add(block);
        }
    }

    /**
     * Removes a block from the cache. Returns an optional boolean if the Honeypot existed and was deleted or not
     *
     * @param block The {@link HoneypotBlockObject} to remove
     * @return True if the removal was successful, false if not (Likely due to it not existing)
     */
    public static boolean removeFromCache(HoneypotBlockObject block) {
        for (HoneypotBlockObject b : cache) {
            if (block.equals(b)) {
                cache.remove(b);
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the Honeypot is in the cache or not
     *
     * @param block The Honeypot to check if it's in cache
     * @return The {@link HoneypotBlockObject} if it's successfully found, null if not
     */
    public static HoneypotBlockObject isInCache(HoneypotBlockObject block) {
        for (HoneypotBlockObject b : cache) {
            if (block.equals(b)) {
                return b;
            }
        }

        return null;
    }

    /**
     * Wipe the cache
     */
    public static void clearCache() {
        cache.clear();
    }

}
