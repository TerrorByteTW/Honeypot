package me.terrorbyte.honeypot.storagemanager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class HoneypotBlockObject {

    private final String coordinates;

    private final String world;

    private final String action;

    /**
     * Create a HoneypotBlockObject
     * 
     * @param block The Block object of the Honeypot
     * @param action The action of the Honeypot
     */
    public HoneypotBlockObject(Block block, String action) {
        this.coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        this.world = block.getWorld().getName();
        this.action = action;
    }

    /**
     * Used for GUI, create a Honeypot based off of strings and not Block objects
     * 
     * @param worldName The world the block is in
     * @param coordinates The coordinates of the block
     * @param action The action of the Honeypot
     */
    public HoneypotBlockObject(String worldName, String coordinates, String action) {
        this.coordinates = coordinates;
        this.world = worldName;
        this.action = action;
    }

    /**
     * Get the String formatted coordinates of the Honeypot
     * 
     * @return Coordinates
     */
    public String getCoordinates() {
        return coordinates;
    }

    /**
     * Get the Location object of the Honeypot
     * 
     * @return Location
     */
    public Location getLocation() {
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(coordinates);
        ArrayList<String> coords = new ArrayList<>();

        while (matcher.find()) {
            String coord = matcher.group();
            coords.add(coord);
        }

        int x = Integer.parseInt(coords.get(0));
        int y = Integer.parseInt(coords.get(1));
        int z = Integer.parseInt(coords.get(2));

        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    /**
     * Get the action of the Honeypot
     * 
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get the world of the Honeypot
     * 
     * @return world
     */
    public String getWorld() {
        return world;
    }

    /**
     * Get the Block object of the Honeypot
     * 
     * @return Honeypot Block object
     */
    public Block getBlock() {
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(coordinates);
        ArrayList<String> coords = new ArrayList<>();

        while (matcher.find()) {
            String coord = matcher.group();
            coords.add(coord);
        }

        int x = Integer.parseInt(coords.get(0));
        int y = Integer.parseInt(coords.get(1));
        int z = Integer.parseInt(coords.get(2));

        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

}
