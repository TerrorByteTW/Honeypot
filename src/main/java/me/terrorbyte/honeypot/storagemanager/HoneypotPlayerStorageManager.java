package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;

public class HoneypotPlayerStorageManager {

    //Create an array list for all honeypotBlocks to reside in while plugin is functioning
    private static final ArrayList<HoneypotPlayerObject> honeypotPlayers = new ArrayList<>();

    //Create a honeypot block by creating a HoneypotBlock object and storing it to the array, then saving it to the file for safe keeping
    public static HoneypotPlayerObject addPlayer(String playerName, int blocksBroken){
        HoneypotPlayerObject honeypotPlayer = new HoneypotPlayerObject(playerName, blocksBroken);
        honeypotPlayers.add(honeypotPlayer);

        try {
            saveHoneypotPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //While we currently don't use this return statement, we might in the future, so I'm leaving it with a compiler warning for now
        return honeypotPlayer;
    }

    //Compare the coordinates of the received block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
    public static void setPlayerCount(String playerName, int blocksBroken) throws IOException {
        for (HoneypotPlayerObject potBreaker : honeypotPlayers){
            if(potBreaker.getPlayerName().equals(playerName)){
                potBreaker.setBlocksBroken(blocksBroken);
                saveHoneypotPlayers();
            }
        }
    }

    //Return the action for the honeypot block (Meant for ban, kick, etc.)
    public static int getCount(String playerName){
        for (HoneypotPlayerObject potBreaker : honeypotPlayers){
            if(potBreaker.getPlayerName().equals(playerName)){
                return potBreaker.getBlocksBroken();
            }
        }
        return -1;
    }

    //Save the list to JSON
    public static void saveHoneypotPlayers() throws IOException {
        switch (Honeypot.getDatabase()) {
            case "json":
                JSONManager.saveHoneypotPlayers(honeypotPlayers);
                break;
            case "sqlite":
                break;
            default:
                break;
        }
    }

    public static void loadHoneypotPlayers(Plugin plugin) throws IOException {
        switch (Honeypot.getDatabase()) {
            case "json":
                JSONManager.loadHoneypotPlayers(plugin);
                break;
            case "sqlite":
                break;
            default:
                break;
        }
    }

}
