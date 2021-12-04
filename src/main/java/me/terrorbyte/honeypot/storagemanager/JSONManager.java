package me.terrorbyte.honeypot.storagemanager;

import com.google.gson.Gson;
import me.terrorbyte.honeypot.Honeypot;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class JSONManager {

    public static ArrayList<HoneypotBlockObject> loadHoneypotBlocks(Plugin plugin) throws IOException {

        ArrayList<HoneypotBlockObject> honeypotBlocks = new ArrayList<>();

        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/HoneypotBlocks.json");
        if (file.exists()){
            Reader reader = new FileReader(file);
            HoneypotBlockObject[] blockList = gson.fromJson(reader, HoneypotBlockObject[].class);
            honeypotBlocks = new ArrayList<>(Arrays.asList(blockList));
        }

        return honeypotBlocks;
    }

    public static void saveHoneypotBlocks(ArrayList<HoneypotBlockObject> blockObjects) throws IOException {
        Gson gson = new Gson();
        File file = new File(Honeypot.getPlugin().getDataFolder().getAbsolutePath() + "/HoneypotBlocks.json");
        file.getParentFile().mkdir();
        file.createNewFile();

        Writer writer = new FileWriter(file, false);
        gson.toJson(blockObjects, writer);
        writer.flush();
        writer.close();
    }

    public static ArrayList<HoneypotPlayerObject> loadHoneypotPlayers(Plugin plugin) throws IOException {

        ArrayList<HoneypotPlayerObject> honeypotPlayers = new ArrayList<>();

        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/HoneypotPlayers.json");
        if (file.exists()){
            Reader reader = new FileReader(file);
            HoneypotPlayerObject[] playerList = gson.fromJson(reader, HoneypotPlayerObject[].class);
            honeypotPlayers = new ArrayList<>(Arrays.asList(playerList));
        }

        return honeypotPlayers;
    }

    public static void saveHoneypotPlayers(ArrayList<HoneypotPlayerObject> playerObjects) throws IOException {
        Gson gson = new Gson();
        File file = new File(Honeypot.getPlugin().getDataFolder().getAbsolutePath() + "/HoneypotPlayers.json");
        file.getParentFile().mkdir();
        file.createNewFile();

        Writer writer = new FileWriter(file, false);
        gson.toJson(playerObjects, writer);
        writer.flush();
        writer.close();
    }

}
