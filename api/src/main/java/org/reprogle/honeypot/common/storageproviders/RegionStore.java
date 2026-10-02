package org.reprogle.honeypot.common.storageproviders;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public interface RegionStore extends Store {
    /**
     * Creates a honeypot region that contains a single block
     * @param block The block to create the honeypot region around
     * @param action The action to perform when the region is triggered
     */
    void createHoneypotRegion(Block block, String action);

    /**
     * Creates a honeypot region that contains multiple blocks
     * @param pos1 The first corner of the region
     * @param pos2 The second corner of the region
     * @param action The action to perform when the region is triggered
     */
    void createHoneypotRegion(Location pos1, Location pos2, String action);

    /**
     * Removes a honeypot region
     * @param location The location of the region to remove
     */
    void removeHoneypotRegion(Location location);

    /**
     * Checks if a location is within a honeypot region
     * @param location The location to check
     * @return True if the location is within a honeypot region, false otherwise
     */
    boolean isHoneypot(Location location);

    /**
     * Gets the honeypot region object at a given location
     * @param location The location to check
     * @return The honeypot region object, or null if no region is found
     */
    HoneypotRegionObject getHoneypotRegion(Location location);

    /**
     * Gets the action associated with a honeypot region at a given location
     * @param location The location to check
     * @return The action string, or null if no region is found
     */
    String getAction(Location location);

    /**
     * Deletes all honeypot regions
     */
    void deleteAllHoneypotRegions();

    /**
     * Gets all honeypot regions
     * @return A list of all honeypot region objects
     */
    List<HoneypotRegionObject> getAllHoneypotRegions();

    /**
     * Gets all honeypot regions within a certain radius of a location
     * @param location The center location to search around
     * @param radius The radius in blocks to search within
     * @return A list of honeypot region objects within the specified radius
     */
    List<HoneypotRegionObject> getNearbyHoneypotRegions(Location location, int radius);

    /**
     * Checks if a region overlaps with any other region
     * @param pos1 The first region's corner location
     * @param pos2 The second region's corner location
     * @return True if the regions overlap, false otherwise
     */
    boolean checkForOverlap(Location pos1, Location pos2);
}
