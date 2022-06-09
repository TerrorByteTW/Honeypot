package me.terrorbyte.honeypot.storagemanager;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

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

    public String getAction() {
        return action;
    }

    public String getWorld(){
        return world;
    }

    public Block getBlock(){
        String[] coords = coordinates.split("-?\\d+");

        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        int z = Integer.parseInt(coords[2]);

        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

}
