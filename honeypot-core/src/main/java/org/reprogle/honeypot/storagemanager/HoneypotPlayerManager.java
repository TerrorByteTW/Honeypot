package org.reprogle.honeypot.storagemanager;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.sqlite.Database;
import org.reprogle.honeypot.storagemanager.sqlite.SQLite;

public class HoneypotPlayerManager {

    private static HoneypotPlayerManager instance = null;

    private HoneypotPlayerManager() {
        // This will be made private in the next version, hence why it's deprecated
    }

    /**
     * Returns the singleton instance of this class
     * 
     * @return The {@link HoneypotPlayerManager} instance
     */
    public static synchronized HoneypotPlayerManager getInstance() {
        if (instance == null)
            instance = new HoneypotPlayerManager();

        return instance;
    }

    /**
     * Create a honeypot block by calling the SQLite DB. In the future this will be
     * a switch case statement to handle
     * multiple DB types
     * 
     * @param player       The Player object
     * @param blocksBroken The amount of Blocks broken
     */
    public void addPlayer(Player player, int blocksBroken) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.createHoneypotPlayer(player, blocksBroken);
        Honeypot.getHoneypotLogger()
                .log("Create Honeypot player: " + player.getName() + ", UUID of: " + player.getUniqueId());
    }

    /**
     * Set the number of blocks broken by the player by calling the SQLite
     * setPlayerCount function. In the future this
     * will be a switch case statement to handle multiple DB types without changing
     * code
     * 
     * @param playerName   The Player object
     * @param blocksBroken The amount of blocks broken by the player
     */
    public void setPlayerCount(Player player, int blocksBroken) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.setPlayerCount(player, blocksBroken);
        Honeypot.getHoneypotLogger().log("Updated Honeypot player: " + player.getName() + ", UUID of: "
                + player.getUniqueId() + ". New count: " + blocksBroken);
    }

    /**
     * Return the action for the honeypot block (Meant for ban, kick, etc.)
     * 
     * @param playerName the Player name
     * @return The amount of Honeypot blocks the player has broken
     */
    public int getCount(Player playerName) {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        return db.getCount(playerName);
    }

    /**
     * Delete's all players in the DB
     */
    public void deleteAllHoneypotPlayers() {
        Database db;
        db = new SQLite(Honeypot.getPlugin());
        db.load();

        db.deleteAllPlayers();
        Honeypot.getHoneypotLogger().log("Deleted all Honeypot players from DB");
    }

}
