package org.reprogle.honeypot.storagemanager;

import java.util.ArrayList;
import java.util.List;

import org.reprogle.honeypot.HoneypotConfigManager;

/**
 * A rudimentary caching utility to lessen dependency on databases used by Honeypot
 */
public class CacheManager {

    private static List<HoneypotBlockObject> cache = new ArrayList<>();

    // Create constructor to hide implicit one
    CacheManager() {

    }

    /**
     * Gets the entire cache list for further processing if necessary
     * @return The List of {@link HoneypotBlockObjects} in the cache
     */
    public static List<HoneypotBlockObject> getCache() {
        return cache;
    }

    /**
     * Adds a {@link HoneypotBlockObject} to the cache
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
     * @param block The {@link HoneypotBlockObject} to remove
     * @return True if the removal was successful, false if not (Likely due to it not existing)
     */
    public static boolean removeFromCache(HoneypotBlockObject block) {
        for (HoneypotBlockObject b : cache) {
            if (b.equals(block)) {
                cache.remove(b);
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the Honeypot is in the cache or not
     * @param block The Honeypot to check if it's in cache
     * @return The {@link HoneypotBlockObject} if it's successfully found, null if not
     */
    public static HoneypotBlockObject isInCache(HoneypotBlockObject block) {
        for (HoneypotBlockObject b : cache) {
            if (b.equals(block)) return b;
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
