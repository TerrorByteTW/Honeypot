package me.terrorbyte.honeypot.storagemanager;

import java.util.UUID;

public class HoneypotPlayerObject {

    private final UUID uuid;
    private int blocksBroken;

    public HoneypotPlayerObject(UUID uuid, int blocksBroken) {
        this.uuid = uuid;
        this.blocksBroken = blocksBroken;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken){
        this.blocksBroken = blocksBroken;
    }

}
