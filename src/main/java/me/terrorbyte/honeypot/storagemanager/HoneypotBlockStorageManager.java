package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.sqlite.Database;
import me.terrorbyte.honeypot.storagemanager.sqlite.SQLite;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;

public class HoneypotBlockStorageManager {

    //Create an array list for all honeypotBlocks to reside in while plugin is functioning
    private static ArrayList<HoneypotBlockObject> honeypotBlocks = new ArrayList<>();
    private static Honeypot plugin;

    //Create a honeypot block by creating a HoneypotBlock object and storing it to the array, then saving it to the file for safe keeping
    public static void createBlock(Block block, String action){
        switch (Honeypot.getDatabase()) {
            case "json" -> {
                HoneypotBlockObject honeypotBlock = new HoneypotBlockObject(block, action, block.getWorld().getName());
                honeypotBlocks.add(honeypotBlock);
                try {
                    saveHoneypotBlocks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "sqlite" -> {
                Database db;
                db = new SQLite(plugin);
                db.createHoneypotBlock(block, action);
            }
            default -> {
            }
        }
    }

    //Compare the coordinates of the received block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
    public static void deleteBlock(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        switch (Honeypot.getDatabase()) {
            case "json":
                for (HoneypotBlockObject honeypot : honeypotBlocks){
                    if(honeypot.getCoordinates().equalsIgnoreCase(coordinates) && block.getWorld().getName().equals(honeypot.getWorldName())){
                        honeypotBlocks.remove(honeypot);
                        try {
                            saveHoneypotBlocks();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                break;
            case "sqlite":
                Database db;
                db = new SQLite(plugin);

                db.removeHoneypotBlock(block);
                break;
            default:
                break;
        }
    }

    //Check if the coordinates of the Honeypot already exist within the list
    public static Boolean isHoneypotBlock(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        switch (Honeypot.getDatabase()) {
            case "json":
                for (HoneypotBlockObject honeypot : honeypotBlocks){
                    if (honeypot.getCoordinates().equalsIgnoreCase(coordinates) && block.getWorld().getName().equals(honeypot.getWorldName())){
                        return true;
                    }
                }
                break;
            case "sqlite":
                Database db;

                db = new SQLite(plugin);
                db.load();

                if (db.isHoneypotBlock(block)) {
                    return true;
                }
                break;
            default:
                return false;
        }

        return false;
    }

    //Return the action for the honeypot block (Meant for ban, kick, etc.)
    public static String getAction(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        switch (Honeypot.getDatabase()) {

            case "json":
                for (HoneypotBlockObject honeypot : honeypotBlocks){
                    if(honeypot.getCoordinates().equalsIgnoreCase((coordinates))){
                        return honeypot.getAction();
                    }
                }
                break;
            case "sqlite":
                Database db;

                db = new SQLite(plugin);
                db.load();

                return db.getAction(block);
            default:
                return null;
        }

        return null;
    }

    //Return the world the block is in
    public static String getWorld(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        switch (Honeypot.getDatabase()) {

            case "json":
                for (HoneypotBlockObject honeypot : honeypotBlocks){
                    if(honeypot.getCoordinates().equalsIgnoreCase((coordinates))){
                        return honeypot.getWorldName();
                    }
                }
                break;
            case "sqlite":
                Database db;

                db = new SQLite(plugin);
                db.load();

                return db.getWorld(block);
            default:
                return null;
        }

        return null;
    }

    //Save the list to JSON
    public static void saveHoneypotBlocks() throws IOException {
        if ("json".equals(Honeypot.getDatabase())) {
            JSONManager.saveHoneypotBlocks(honeypotBlocks);
        }
    }

    //Retrieve JSON List
    public static void loadHoneypotBlocks(Plugin plugin) throws IOException {
        if ("json".equals(Honeypot.getDatabase())) {
            honeypotBlocks = JSONManager.loadHoneypotBlocks(plugin);
        }
    }
}
