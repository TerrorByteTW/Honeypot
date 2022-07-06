package org.reprogle.honeypot.storagemanager;

import org.bukkit.block.Block;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.sqlite.Database;
import org.reprogle.honeypot.storagemanager.sqlite.SQLite;

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
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();
        
        db.createHoneypotBlock(block, action);
    }

    /**
     * Compare the coordinates of the received {@link Block} to the DB. If it exists, delete it and break
     * to avoid a Java error
     * 
     * @param block The Honeypot {@link Block} we're deleting
     */
    public void deleteBlock(Block block) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();
        
        db.removeHoneypotBlock(block);
    }

    /**
     * Check if the coordinates of the Honeypot already exist within the list
     * 
     * @param block The {@link Block} we're checking
     * @return true or false
     */
    public boolean isHoneypotBlock(Block block) {
        Database db;

        db = new SQLite(Honeypot.getPlugin());
        db.load();

        return db.isHoneypotBlock(block);
    }

    /**
     * Return the action for the honeypot {@link Block} (Meant for ban, kick, etc.)
     * 
     * @param block The Block we're checking
     * @return The Honeypot's action as a string
     */
    public String getAction(Block block) {
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
