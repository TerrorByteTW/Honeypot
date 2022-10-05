package org.reprogle.honeypot.storagemanager;

import java.util.List;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.sqlite.Database;
import org.reprogle.honeypot.storagemanager.sqlite.SQLite;

/**
 * A class for managing Honeypot history entries.
 * Adds functions for creating, removing, querying, and purging the history
 * database.
 * 
 * @see HoneypotPlayerHistoryObject
 */
public class HoneypotPlayerHistoryManager {

    private static HoneypotPlayerHistoryManager instance = null;

    /**
     * Creates an instance of the HoneypotPlayerHistoryManager
     * @deprecated
     * This function is now deprecated as this class is intended to be used as a singleton. Please use {@link #getInstance()} instead
     * The immediate next version of Honeypot will make this function private.
     */
    @Deprecated
    public HoneypotPlayerHistoryManager() {
        // This will be made private in the next version, hence why it's deprecated
    }

    /**
     * Returns the singleton instance of this class
     * @return The {@link HoneypotPlayerHistoryManager} instance
     */
    // Warnings are suppressed due to the deprecation of the public HoneypotBlockManager constructor
    @SuppressWarnings("java:S1874")
    public static HoneypotPlayerHistoryManager getInstance() {
        if (instance == null)
            instance = new HoneypotPlayerHistoryManager();

        return instance;
    }

    /**
     * Add an entry to the player history table
     * 
     * @param p The player to add
     * @param b The honeypot block they triggered
     */
    public void addPlayerHistory(Player p, HoneypotBlockObject b) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.addPlayerHistory(p, b);

        Honeypot.getHoneypotLogger().log("Added new history entry for player " + p.getName());
    }

    /**
     * Get the history for a player
     * 
     * @param p The player to grab history for
     * @return A list of all HoneypotPlayerHistory objects
     */
    public List<HoneypotPlayerHistoryObject> getPlayerHistory(Player p) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        return db.retrieveHistory(p);
    }

    /**
     * Delete all history for a particular player. An optional n parameter for
     * specifying the number of most recent rows to delete
     * 
     * @param p The player to delete
     * @param n Optional, the number of most recent rows
     */
    public void deletePlayerHistory(Player p, int... n) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        if (n.length > 0) {
            db.deletePlayerHistory(p, n);
        } else {
            db.deletePlayerHistory(p);
        }

        Honeypot.getHoneypotLogger().log("Deleting player history for player " + p.getName());
    }

    /**
     * A function to purge the entire history table
     */
    public void deleteAllHistory() {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.deleteAllHistory();
    }

}
