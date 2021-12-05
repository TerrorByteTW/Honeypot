package me.terrorbyte.honeypot.storagemanager;

import org.bukkit.block.Block;

public class HoneypotBlockObject {

    private final String coordinates;
    private final String action;
    private final String worldName;

    public HoneypotBlockObject(Block block, String action, String worldName) {
        this.coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        this.action = action;
        this.worldName = worldName;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getAction() {
        return action;
    }

    public String getWorldName() {
        return worldName;
    }

}
