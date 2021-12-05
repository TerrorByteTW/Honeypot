package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.storagemanager.sqlite.Database;
import me.terrorbyte.honeypot.storagemanager.sqlite.SQLite;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;

public class HoneypotPlayerStorageManager {

    private static Honeypot plugin;

    //Create an array list for all honeypotBlocks to reside in while plugin is functioning
    private static ArrayList<HoneypotPlayerObject> honeypotPlayers = new ArrayList<>();

    //Create a honeypot block by creating a HoneypotBlock object and storing it to the array, then saving it to the file for safe keeping
    public static void addPlayer(String playerName, int blocksBroken){
        switch (Honeypot.getDatabase()) {
            case "json" -> {
                HoneypotPlayerObject honeypotPlayer = new HoneypotPlayerObject(playerName, blocksBroken);
                honeypotPlayers.add(honeypotPlayer);
                try {
                    saveHoneypotPlayers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "sqlite" -> {
                Database db;
                db = new SQLite(plugin);
                db.createHoneypotPlayer(playerName, blocksBroken);
            }
            default -> {
            }
        }
    }

    //Compare the coordinates of the received block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
    public static void setPlayerCount(String playerName, int blocksBroken) throws IOException {
        switch (Honeypot.getDatabase()) {
            case "json":
                for (HoneypotPlayerObject potBreaker : honeypotPlayers){
                    if(potBreaker.getPlayerName().equals(playerName)){
                        potBreaker.setBlocksBroken(blocksBroken);
                        saveHoneypotPlayers();
                    }
                }
                break;
            case "sqlite":
                Database db;
                db = new SQLite(plugin);

                db.setPlayerCount(playerName, blocksBroken);

                break;
            default:
                break;
        }

    }

    //Return the action for the honeypot block (Meant for ban, kick, etc.)
    public static int getCount(String playerName){
        switch (Honeypot.getDatabase()) {
            case "json" -> {
                for (HoneypotPlayerObject potBreaker : honeypotPlayers) {
                    if (potBreaker.getPlayerName().equals(playerName)) {
                        return potBreaker.getBlocksBroken();
                    }
                }
                return -1;
            }
            case "sqlite" -> {
                Database db;
                db = new SQLite(plugin);
                return db.getCount(playerName);
            }
            default -> {
            }
        }

        return -1;
    }

    //Save the list to JSON
    public static void saveHoneypotPlayers() throws IOException {
        if ("json".equals(Honeypot.getDatabase())) {
            JSONManager.saveHoneypotPlayers(honeypotPlayers);
        }
    }

    public static void loadHoneypotPlayers(Plugin plugin) throws IOException {
        if ("json".equals(Honeypot.getDatabase())) {
            honeypotPlayers = JSONManager.loadHoneypotPlayers(plugin);
        }
    }

}
