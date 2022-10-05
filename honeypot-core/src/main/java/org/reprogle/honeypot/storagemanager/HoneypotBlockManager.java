package org.reprogle.honeypot.storagemanager;

import org.bukkit.block.Block;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.sqlite.Database;
import org.reprogle.honeypot.storagemanager.sqlite.SQLite;

import java.util.List;

public class HoneypotBlockManager {

    private static HoneypotBlockManager instance = null;

    private HoneypotBlockManager() {
    }

    /**
     * Returns the singleton instance of this class
     * @return The {@link HoneypotBlockManager} instance
     */
    public static HoneypotBlockManager getInstance() {
        if (instance == null)
            instance = new HoneypotBlockManager();

        return instance;
    }

    /**
     * Create a Honeypot {@link Block} and add it to the DB
     * 
     * @param block  The Honeypot Block we're creating
     * @param action The action of the Honeypot
     */
    @SuppressWarnings("java:S1604")
    public void createBlock(Block block, String action) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.createHoneypotBlock(block, action);
        CacheManager.addToCache(new HoneypotBlockObject(block, action));

        Honeypot.getHoneypotLogger().log("Created Honeypot block with action " + action + " at " + block.getX() + ", "
                + block.getY() + ", " + block.getZ());
    }

    /**
     * Delete a block from the Honeypot DB
     * 
     * @param block The Honeypot {@link Block} we're deleting
     */
    public void deleteBlock(Block block) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.removeHoneypotBlock(block);
        CacheManager.removeFromCache(new HoneypotBlockObject(block, null));

        Honeypot.getHoneypotLogger()
                .log("Deleted Honeypot block with at " + block.getX() + ", " + block.getY() + ", " + block.getZ());
    }

    /**
     * Check if the block is a Honeypot block
     * 
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public boolean isHoneypotBlock(Block block) {
        if (CacheManager.isInCache(new HoneypotBlockObject(block, null)) != null)
            return true;

        Database db;

        db = new SQLite(Honeypot.getPlugin());
        db.load();

        if (Boolean.TRUE.equals(db.isHoneypotBlock(block))) {
            String action = getAction(block);
            CacheManager.addToCache(new HoneypotBlockObject(block, action));
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

        if (Boolean.TRUE.equals(isHoneypotBlock(block)))
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
        HoneypotBlockObject potential = CacheManager.isInCache(new HoneypotBlockObject(block, null));

        if (potential != null)
            return potential.getAction();

        Database db;

        db = new SQLite(Honeypot.getPlugin());
        db.load();

        return db.getAction(block);
    }

    /**
     * Delete all Honeypots in the entire DB
     */
    public void deleteAllHoneypotBlocks() {
        Database db;

        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.deleteAllBlocks();
        CacheManager.clearCache();

        Honeypot.getHoneypotLogger().log("Deleted all Honeypot blocks!");
    }

    /**
     * Get all {@link HoneypotBlockObject} in the DB
     * 
     * @return An array list of all HoneypotBlockObjects
     */
    public List<HoneypotBlockObject> getAllHoneypots() {
        Database db;

        db = new SQLite(Honeypot.getPlugin());
        db.load();

        return db.getAllHoneypots();
    }
}
