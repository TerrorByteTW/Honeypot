package me.terrorbyte.honeypot.storagemanager;

import org.bukkit.block.Block;

public class HoneypotBlockObject {

    private final String coordinates;
    private final String action;

    public HoneypotBlockObject(Block block, String action) {
        this.coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        this.action = action;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getAction() {
        return action;
    }

}
