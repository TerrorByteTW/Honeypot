package org.reprogle.honeypot.storagemanager;

import org.bukkit.block.Block;

import java.util.List;

public class HoneypotBlockManager {

    /**
     * Create a honeypot {@link Block} by creating a HoneypotBlock object and storing it to DB.
     * 
     * @param block The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    @SuppressWarnings("java:S1604")
    public void createBlock(Block block, String action) {
        /*
         * Function intentionally left blank as it's just a placeholder to develop against Honeypot
         */
    }

    /**
     * Compare the coordinates of the received {@link Block} to the DB. If it exists, delete it and break to avoid a
     * Java error
     * 
     * @param block The Honeypot {@link Block} we're deleting
     */
    public void deleteBlock(Block block) {
        /*
         * Function intentionally left blank as it's just a placeholder to develop against Honeypot
         */
    }

    /**
     * Check if the coordinates of the Honeypot already exist within the list
     * 
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public boolean isHoneypotBlock(Block block) {
        return isHoneypotBlock(block);
    }

    /**
     * Return the action for the honeypot {@link Block} (Meant for ban, kick, etc.)
     * 
     * @param block The Block we're checking
     * @return The Honeypot's action as a string
     */
    public String getAction(Block block) {
        return getAction(block);
    }

    /**
     * Delete all Honeypots in the entire DB. Do not use unless you know what you're doing
     */
    public void deleteAllHoneypotBlocks() {
        /*
         * Function intentionally left blank as it's just a placeholder to develop against Honeypot
         */
    }

    /**
     * Get all {@link HoneypotBlockObject} in the DB
     * 
     * @return An array list of all HoneypotBlockObjects
     */
    public List<HoneypotBlockObject> getAllHoneypots() {
        return getAllHoneypots();
    }
}
