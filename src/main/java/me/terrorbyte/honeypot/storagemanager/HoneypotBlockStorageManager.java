package me.terrorbyte.honeypot.storagemanager;

import me.terrorbyte.honeypot.Honeypot;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;

public class HoneypotBlockStorageManager {

    //Create an array list for all honeypotBlocks to reside in while plugin is functioning
    private static final ArrayList<HoneypotBlockObject> honeypotBlocks = new ArrayList<>();

    //Create a honeypot block by creating a HoneypotBlock object and storing it to the array, then saving it to the file for safe keeping
    public static HoneypotBlockObject createBlock(Block block, String action){
        HoneypotBlockObject honeypotBlock = new HoneypotBlockObject(block, action);
        honeypotBlocks.add(honeypotBlock);

        try {
            saveHoneypotBlocks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //While we currently don't use this return statement, we might in the future, so I'm leaving it with a compiler warning for now
        return honeypotBlock;
    }

    //Compare the coordinates of the received block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
    public static void deleteBlock(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        for (HoneypotBlockObject honeypot : honeypotBlocks){
            if(honeypot.getCoordinates().equalsIgnoreCase(coordinates)){
                honeypotBlocks.remove(honeypot);
                try {
                    saveHoneypotBlocks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    //Check if the coordinates of the Honeypot already exist within the list
    public static Boolean isHoneypotBlock(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        for (HoneypotBlockObject honeypot : honeypotBlocks){
            if (honeypot.getCoordinates().equalsIgnoreCase(coordinates)){
                return true;
            }
        }
        return false;
    }

    //Return the action for the honeypot block (Meant for ban, kick, etc.)
    public static String getAction(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        for (HoneypotBlockObject honeypot : honeypotBlocks){
            if(honeypot.getCoordinates().equalsIgnoreCase((coordinates))){
                return honeypot.getAction();
            }
        }

        return null;
    }

    //Save the list to JSON
    public static void saveHoneypotBlocks() throws IOException {
        switch (Honeypot.getDatabase()) {
            case "json":
                JSONManager.saveHoneypotBlocks(honeypotBlocks);
                break;
            case "sqlite":
                break;
            default:
                break;
        }
    }

    //Retrieve JSON List
    public static void loadHoneypotBlocks(Plugin plugin) throws IOException {
        switch (Honeypot.getDatabase()) {
            case "json":
                JSONManager.loadHoneypotBlocks(plugin);
                break;
            case "sqlite":
                break;
            default:
                break;
        }
    }
}
