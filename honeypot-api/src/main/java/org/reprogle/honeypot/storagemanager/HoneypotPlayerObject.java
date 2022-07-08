package org.reprogle.honeypot.storagemanager;

import java.util.UUID;

/**
 * A class representing a Honeypot Player object. 
 * Includes methods for getting all values of a Honeypot player object, which can be returned via the {@link HoneypotPlayerManager} class
 * @see HoneypotPlayerManager
 * @see HoneypotBlockManager
 * @see HoneypotBlockObject
 */
@SuppressWarnings({"unused", "java:S116"})
public abstract class HoneypotPlayerObject {

    private final UUID UUID;

    private int blocksBroken;

    /**
     * Create a HoneypotPlayerObject
     * 
     * @param uuid The UUID of the player
     * @param blocksBroken How many blocks the player has broken
     */
    protected HoneypotPlayerObject(UUID uuid, int blocksBroken) {
        this.UUID = uuid;
        this.blocksBroken = blocksBroken;
    }

    /**
     * Get the UUID of the player
     * 
     * @return Player's UUID
     */
    public abstract UUID getUUID();

    /**
     * Get's the number of blocks broken by the player
     * 
     * @return Amount of blocks broken
     */
    public abstract int getBlocksBroken();

    /**
     * Set's the amount of blocks broken
     * 
     * @param blocksBroken The number of blocks the player has broken
     */
    public abstract void setBlocksBroken(int blocksBroken);

}
