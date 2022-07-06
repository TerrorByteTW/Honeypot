package org.reprogle.honeypot.storagemanager;

import org.bukkit.entity.Player;

public abstract class HoneypotPlayerManager {

    /**
     * Create a private constructor to hide the implicit one
     * 
     * SonarLint rule java:S1118
     */
    private HoneypotPlayerManager() {

    }

    /**
     * Create a honeypot block by calling the SQLite DB. In the future this will be a switch case statement to handle
     * multiple DB types
     * 
     * @param player The Player object
     * @param blocksBroken The amount of Blocks broken
     */
    public abstract void addPlayer(Player player, int blocksBroken);

    /**
     * Set the number of blocks broken by the player by calling the SQLite setPlayerCount function. In the future this
     * will be a switch case statement to handle multiple DB types without changing code
     * 
     * @param playerName The Player object
     * @param blocksBroken The amount of blocks broken by the player
     */
    public abstract void setPlayerCount(Player playerName, int blocksBroken);

    /**
     * Return the action for the honeypot block (Meant for ban, kick, etc.)
     * 
     * @param playerName the Player name
     * @return The amount of Honeypot blocks the player has broken
     */
    public abstract int getCount(Player playerName);

    /**
     * Delete's all players in the DB
     */
    public abstract void deleteAllHoneypotPlayers();

}
