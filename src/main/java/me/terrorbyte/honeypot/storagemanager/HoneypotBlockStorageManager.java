package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.sqlite.Database;
import me.terrorbyte.honeypot.storagemanager.sqlite.SQLite;
import org.bukkit.block.Block;

import java.io.*;

public class HoneypotBlockStorageManager {

    //Create an array list for all honeypotBlocks to reside in while plugin is functioning
    private static final Honeypot plugin = Honeypot.getPlugin();

    //Create a honeypot block by creating a HoneypotBlock object and storing it to the array, then saving it to the file for safe keeping
    public static void createBlock(Block block, String action){
        Database db;
        db = new SQLite(plugin);
        db.createHoneypotBlock(block, action);
    }

    //Compare the coordinates of the received block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
    public static void deleteBlock(Block block){
        Database db;
        db = new SQLite(plugin);
        db.removeHoneypotBlock(block);
    }

    //Check if the coordinates of the Honeypot already exist within the list
    public static Boolean isHoneypotBlock(Block block){
        Database db;

        db = new SQLite(plugin);
        db.load();

        if (db.isHoneypotBlock(block)) {
            return true;
        } else {
            return false;
        }
    }

    //Return the action for the honeypot block (Meant for ban, kick, etc.)
    public static String getAction(Block block){
        Database db;

        db = new SQLite(plugin);
        db.load();

        return db.getAction(block);
    }

    public static void deleteAllHoneypotBlocks() throws IOException {
        Database db;

        db = new SQLite(plugin);
        db.deleteAllBlocks();
    }
}
