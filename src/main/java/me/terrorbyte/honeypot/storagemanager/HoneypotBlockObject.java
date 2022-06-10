package me.terrorbyte.honeypot.storagemanager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import me.terrorbyte.honeypot.Honeypot;

public class HoneypotBlockObject {

    private final String coordinates;
    private final String world;
    private final String action;

    public HoneypotBlockObject(Block block, String action) {
        this.coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        this.world = block.getWorld().getName();
        this.action = action;
    }

    public HoneypotBlockObject(String worldName, String coordinates, String action) {
        this.coordinates = coordinates;
        this.world = worldName;
        this.action = action;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public Location getLocation() {
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(coordinates);
        ArrayList<String> coords = new ArrayList<String>();

        while(matcher.find()){
            String coord = matcher.group();
            coords.add(coord);
        }

        int x = Integer.parseInt(coords.get(0));
        int y = Integer.parseInt(coords.get(1));
        int z = Integer.parseInt(coords.get(2));

        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public String getAction() {
        return action;
    }

    public String getWorld(){
        return world;
    }

    public Block getBlock(){
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(coordinates);
        ArrayList<String> coords = new ArrayList<String>();

        while(matcher.find()){
            String coord = matcher.group();
            coords.add(coord);
        }

        int x = Integer.parseInt(coords.get(0));
        int y = Integer.parseInt(coords.get(1));
        int z = Integer.parseInt(coords.get(2));

        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

}
