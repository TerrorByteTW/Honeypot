package me.terrorbyte.honeypot.storagemanager;

import com.google.gson.Gson;
import me.terrorbyte.honeypot.Honeypot;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class HoneypotBlockStorageManager {

    //Create an array list for all honeypotBlocks to reside in while plugin is functioning
    private static ArrayList<HoneypotBlockObject> honeypotBlocks = new ArrayList<>();

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

    //Compare the coordinates of the sent block to every block in the JSON list. If it exists, delete it and break to avoid a Java error
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
        Gson gson = new Gson();
        File file = new File(Honeypot.getPlugin().getDataFolder().getAbsolutePath() + "/HoneypotBlocks.json");
        file.getParentFile().mkdir();
        file.createNewFile();

        Writer writer = new FileWriter(file, false);
        gson.toJson(honeypotBlocks, writer);
        writer.flush();
        writer.close();
    }

    //Retrieve the JSON and store it in a list
    public static void loadHoneypotBlocks() throws IOException {
        Gson gson = new Gson();
        File file = new File(Honeypot.getPlugin().getDataFolder().getAbsolutePath() + "/HoneypotBlocks.json");
        if (file.exists()){
            Reader reader = new FileReader(file);
            HoneypotBlockObject[] blockList = gson.fromJson(reader, HoneypotBlockObject[].class);
            honeypotBlocks = new ArrayList<>(Arrays.asList(blockList));
        }
    }

    public static void loadHoneypotBlocks(Plugin plugin) throws IOException {
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/HoneypotBlocks.json");
        if (file.exists()){
            Reader reader = new FileReader(file);
            HoneypotBlockObject[] blockList = gson.fromJson(reader, HoneypotBlockObject[].class);
            honeypotBlocks = new ArrayList<>(Arrays.asList(blockList));
        }
    }

}
