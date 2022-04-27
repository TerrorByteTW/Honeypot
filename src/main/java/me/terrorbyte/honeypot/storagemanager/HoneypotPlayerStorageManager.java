package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.sqlite.Database;
import me.terrorbyte.honeypot.storagemanager.sqlite.SQLite;
import org.bukkit.entity.Player;

import java.io.*;

public class HoneypotPlayerStorageManager {

    private static Honeypot plugin;

    //Create a honeypot block by creating a HoneypotBlock object and storing it to the array, then saving it to the file for safe keeping
    public static void addPlayer(Player player, int blocksBroken){
        Database db;
        db = new SQLite(plugin);
        db.createHoneypotPlayer(player, blocksBroken);
    }

    //Compare the coordinates of the received block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
    public static void setPlayerCount(Player playerName, int blocksBroken) throws IOException {
        Database db;
        db = new SQLite(plugin);
        db.setPlayerCount(playerName, blocksBroken);
    }

    //Return the action for the honeypot block (Meant for ban, kick, etc.)
    public static int getCount(Player playerName){
        Database db;
        db = new SQLite(plugin);
        return db.getCount(playerName);
    }

    public static void deleteAllHoneypotPlayers() throws IOException {
        Database db;

        db = new SQLite(plugin);
        db.deleteAllPlayers();
    }

}
