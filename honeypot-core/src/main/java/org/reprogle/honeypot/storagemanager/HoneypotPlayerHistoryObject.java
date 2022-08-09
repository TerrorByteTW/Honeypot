package org.reprogle.honeypot.storagemanager;

import org.bukkit.entity.Player;

@SuppressWarnings("java:S116")
public class HoneypotPlayerHistoryObject {
    private String dateTime;
    private String player;
    private String UUID;
    private HoneypotBlockObject hbo;

    /**
     * @param dateTime
     * @param player
     * @param hbo
     */
    public HoneypotPlayerHistoryObject(String dateTime, String player, String UUID, HoneypotBlockObject hbo) {
        this.dateTime = dateTime;
        this.player = player;
        this.UUID = UUID;
        this.hbo = hbo;
    }

    /**
     * @param dateTime
     * @param player
     * @param hbo
     */
    public HoneypotPlayerHistoryObject(String dateTime, Player player, HoneypotBlockObject hbo) {
        this.dateTime = dateTime;
        this.player = player.getName();
        this.UUID = player.getUniqueId().toString();
        this.hbo = hbo;
    }

    /**
     * @return
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * @return
     */
    public String getPlayer() {
        return player;
    }

    public String getUUID() {
        return UUID;
    }

    /**
     * @return
     */
    public HoneypotBlockObject getHoneypot() {
        return hbo;
    }

    
}
