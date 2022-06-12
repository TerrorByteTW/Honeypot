package me.terrorbyte.honeypot.storagemanager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class HoneypotBlockObject {

    private final String COORDINATES;
    private final String WORLD;
    private final String ACTION;

    /**
     * Create a HoneypotBlockObject
     * @param block The Block object of the Honeypot
     * @param action The action of the Honeypot
     */
    public HoneypotBlockObject(Block block, String action) {
        this.COORDINATES = block.getX() + ", " + block.getY() + ", " + block.getZ();
        this.WORLD = block.getWorld().getName();
        this.ACTION = action;
    }

    /**
     * Used for GUI, create a Honeypot based off of strings and not Block objects
     * @param worldName The world the block is in
     * @param coordinates The coordinates of the block
     * @param action The action of the Honeypot
     */
    public HoneypotBlockObject(String worldName, String coordinates, String action) {
        this.COORDINATES = coordinates;
        this.WORLD = worldName;
        this.ACTION = action;
    }

    /**
     * Get the String formatted coordinates of the Honeypot
     * @return Coordinates
     */
    public String getCoordinates() {
        return COORDINATES;
    }

    /**
     * Get the Location object of the Honeypot
     * @return Location
     */
    public Location getLocation() {
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(COORDINATES);
        ArrayList<String> coords = new ArrayList<String>();

        while(matcher.find()){
            String coord = matcher.group();
            coords.add(coord);
        }

        int x = Integer.parseInt(coords.get(0));
        int y = Integer.parseInt(coords.get(1));
        int z = Integer.parseInt(coords.get(2));

        return new Location(Bukkit.getWorld(WORLD), x, y, z);
    }

    /**
     * Get the action of the Honeypot
     * @return action
     */
    public String getAction() {
        return ACTION;
    }

    /**
     * Get the world of the Honeypot
     * @return world
     */
    public String getWorld(){
        return WORLD;
    }

    /**
     * Get the Block object of the Honeypot
     * @return Honeypot Block object
     */
    public Block getBlock(){
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(COORDINATES);
        ArrayList<String> coords = new ArrayList<String>();

        while(matcher.find()){
            String coord = matcher.group();
            coords.add(coord);
        }

        int x = Integer.parseInt(coords.get(0));
        int y = Integer.parseInt(coords.get(1));
        int z = Integer.parseInt(coords.get(2));

        return Bukkit.getWorld(WORLD).getBlockAt(x, y, z);
    }

}
