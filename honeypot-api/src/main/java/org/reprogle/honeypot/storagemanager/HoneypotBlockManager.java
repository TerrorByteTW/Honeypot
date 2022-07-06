package org.reprogle.honeypot.storagemanager;

import org.bukkit.block.Block;

import java.util.List;

public abstract class HoneypotBlockManager {

    /**
     * Create a private constructor to hide the implicit one
     * 
     * SonarLint rule java:S1118
     */
    private HoneypotBlockManager() {

    }

    /**
     * Create a honeypot {@link Block} by creating a HoneypotBlock object and storing it to DB.
     * 
     * @param block The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    @SuppressWarnings("java:S1604")
    public abstract void createBlock(Block block, String action);

    /**
     * Compare the coordinates of the received {@link Block} to the DB. If it exists, delete it and break
     * to avoid a Java error
     * 
     * @param block The Honeypot {@link Block} we're deleting
     */
    public abstract void deleteBlock(Block block);

    /**
     * Check if the coordinates of the Honeypot already exist within the list
     * 
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public abstract boolean isHoneypotBlock(Block block);

    /**
     * Return the action for the honeypot {@link Block} (Meant for ban, kick, etc.)
     * 
     * @param block The Block we're checking
     * @return The Honeypot's action as a string
     */
    public abstract String getAction(Block block);

    /**
     * Delete all Honeypots in the entire DB
     */
    public abstract void deleteAllHoneypotBlocks();

    /**
     * Get all {@link HoneypotBlockObject} in the DB
     * 
     * @return An array list of all HoneypotBlockObjects
     */
    public abstract List<HoneypotBlockObject> getAllHoneypots();
}
