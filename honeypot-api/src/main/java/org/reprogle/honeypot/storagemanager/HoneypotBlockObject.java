package org.reprogle.honeypot.storagemanager;

import org.bukkit.Location;
import org.bukkit.block.Block;

@SuppressWarnings("unused")
public abstract class HoneypotBlockObject {

    private final String coordinates;

    private final String world;

    private final String action;

    /**
     * Create a HoneypotBlockObject
     * 
     * @param block The Block object of the Honeypot
     * @param action The action of the Honeypot
     */
    protected HoneypotBlockObject(Block block, String action) {
        this.coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        this.world = block.getWorld().getName();
        this.action = action;
    }

    /**
     * Used for GUI, create a Honeypot based off of strings and not Block objects
     * 
     * @param worldName The world the block is in
     * @param coordinates The coordinates of the block
     * @param action The action of the Honeypot
     */
    protected HoneypotBlockObject(String worldName, String coordinates, String action) {
        this.coordinates = coordinates;
        this.world = worldName;
        this.action = action;
    }

    /**
     * Get the String formatted coordinates of the Honeypot
     * 
     * @return Coordinates
     */
    public abstract String getCoordinates();

    /**
     * Get the Location object of the Honeypot
     * 
     * @return Location
     */
    public abstract Location getLocation();

    /**
     * Get the action of the Honeypot
     * 
     * @return action
     */
    public abstract String getAction();

    /**
     * Get the world of the Honeypot
     * 
     * @return world
     */
    public abstract String getWorld();

    /**
     * Get the Block object of the Honeypot
     * 
     * @return Honeypot Block object
     */
    public abstract Block getBlock();

}
