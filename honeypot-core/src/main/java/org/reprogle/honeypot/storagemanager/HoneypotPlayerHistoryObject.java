package org.reprogle.honeypot.storagemanager;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HoneypotPlayerHistoryObject {
    private String dateTime;
    private String player;
    private String UUID;
    private HoneypotBlockObject hbo;

    /**
     * @param dateTime
     * @param player
     * @param coordinates
     * @param world
     * @param action
     */
    public HoneypotPlayerHistoryObject(String dateTime, String player, String UUID, String coordinates, String world, String action) {
        this.dateTime = dateTime;
        this.player = player;
        this.UUID = UUID;
        this.hbo = new HoneypotBlockObject(world, coordinates, action);
    }

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
     * @param block
     * @param action
     */
    public HoneypotPlayerHistoryObject(String dateTime, String player, String UUID, Block block, String action) {
        this.dateTime = dateTime;
        this.player = player;
        this.UUID = UUID;
        this.hbo = new HoneypotBlockObject(block, action);
    }

    /**
     * @param dateTime
     * @param player
     * @param block
     * @param action
     */
    public HoneypotPlayerHistoryObject(String dateTime, Player player, Block block, String action) {
        this.dateTime = dateTime;
        this.player = player.getName();
        this.UUID = player.getUniqueId().toString();
        this.hbo = new HoneypotBlockObject(block, action);
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
