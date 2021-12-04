package me.terrorbyte.honeypot.storagemanager;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HoneypotPlayerObject {

    private final String playerName;
    private int blocksBroken;

    public HoneypotPlayerObject(String playerName, int blocksBroken) {
        this.playerName = playerName;
        this.blocksBroken = blocksBroken;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken){
        this.blocksBroken = blocksBroken;
    }

}
