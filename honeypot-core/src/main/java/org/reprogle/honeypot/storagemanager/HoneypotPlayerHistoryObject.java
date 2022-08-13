package org.reprogle.honeypot.storagemanager;

import org.bukkit.entity.Player;

/**
 * A class representing a player history entry. 
 * Includes methods for getting all values of a Honeypot history entry, which can be returned via the {@link HoneypotPlayerHistoryManager} class
 * @see HoneypotPlayerHistoryManager
 */
@SuppressWarnings("java:S116")
public class HoneypotPlayerHistoryObject {
    private String dateTime;
    private String player;
    private String UUID;
    private HoneypotBlockObject hbo;

    /**
     * Constructor for creating a history entry
     * @param dateTime The Date and Time in string format. Really need to improve this to have standards but oh well
     * @param player The player's name
     * @param UUID The UUID of the player
     * @param hbo The HoneypotBlockObject they broke
     */
    public HoneypotPlayerHistoryObject(String dateTime, String player, String UUID, HoneypotBlockObject hbo) {
        this.dateTime = dateTime;
        this.player = player;
        this.UUID = UUID;
        this.hbo = hbo;
    }

    /**
     * Constructor for creating a history entry
     * @param dateTime The Date and Time in string format. Really need to improve this to have standards but oh well
     * @param player The player object
     * @param hbo The HoneypotBlockObject they broke
     */
    public HoneypotPlayerHistoryObject(String dateTime, Player player, HoneypotBlockObject hbo) {
        this.dateTime = dateTime;
        this.player = player.getName();
        this.UUID = player.getUniqueId().toString();
        this.hbo = hbo;
    }

    /**
     * Get the date and time of the history entry
     * @return Date and Time, string formatted and in GMT +0:00
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Get the player's name
     * @return Player name
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Get the player's UUID
     * @return UUID
     */
    public String getUUID() {
        return UUID;
    }

    /** Get the Honeypot they broke
     * @return {@link HoneypotBlockObject}
     */
    public HoneypotBlockObject getHoneypot() {
        return hbo;
    }

    
}
