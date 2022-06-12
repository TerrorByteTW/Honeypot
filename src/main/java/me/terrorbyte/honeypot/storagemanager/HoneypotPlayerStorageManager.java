package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.sqlite.Database;
import me.terrorbyte.honeypot.storagemanager.sqlite.SQLite;
import org.bukkit.entity.Player;

import java.io.*;

public class HoneypotPlayerStorageManager {

    private static Honeypot plugin;

    /**
     * Create a honeypot block by calling the SQLite DB. In the future this will be a switch case statement to handle multiple DB types
     * @param player The Player object
     * @param blocksBroken The amount of Blocks broken
     */
    public static void addPlayer(Player player, int blocksBroken){
        Database db;
        db = new SQLite(plugin);
        db.createHoneypotPlayer(player, blocksBroken);
    }

    /**
     * Set the number of blocks broken by the player by calling the SQLite setPlayerCount function. 
     * In the future this will be a switch case statement to handle multiple DB types without changing code 
     * @param playerName The Player object
     * @param blocksBroken The amount of blocks broken by the player
     * @throws IOException
     */
    public static void setPlayerCount(Player playerName, int blocksBroken) throws IOException {
        Database db;
        db = new SQLite(plugin);
        db.setPlayerCount(playerName, blocksBroken);
    }

    /**
     * Return the action for the honeypot block (Meant for ban, kick, etc.)
     * @param playerName the Player name
     * @return The amount of Honeypot blocks the player has broken
     */
    public static int getCount(Player playerName){
        Database db;
        db = new SQLite(plugin);
        return db.getCount(playerName);
    }

    /**
     * Delete's all players in the DB
     * @throws IOException
     */
    public static void deleteAllHoneypotPlayers() throws IOException {
        Database db;

        db = new SQLite(plugin);
        db.deleteAllPlayers();
    }

}
