package org.reprogle.honeypot.storagemanager;

import java.util.UUID;

public class HoneypotPlayerObject {

    private final UUID UUID;

    private int blocksBroken;

    /**
     * Create a HoneypotPlayerObject
     * 
     * @param uuid The UUID of the player
     * @param blocksBroken How many blocks the player has broken
     */
    public HoneypotPlayerObject(UUID uuid, int blocksBroken) {
        this.UUID = uuid;
        this.blocksBroken = blocksBroken;
    }

    /**
     * Get the UUID of the player
     * 
     * @return Player's UUID
     */
    public UUID getUUID() {
        return UUID;
    }

    /**
     * Get's the number of blocks broken by the player
     * 
     * @return Amount of blocks broken
     */
    public int getBlocksBroken() {
        return blocksBroken;
    }

    /**
     * Set's the amount of blocks broken
     * 
     * @param blocksBroken The number of blocks the player has broken
     */
    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

}
