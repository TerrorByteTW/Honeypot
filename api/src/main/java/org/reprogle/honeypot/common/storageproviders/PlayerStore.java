package org.reprogle.honeypot.common.storageproviders;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PlayerStore extends Store {
    /**
     * Adds a player to the storage provider with the specified number of blocks broken.
     *
     * @param player       The player to add.
     * @param blocksBroken The number of blocks broken by the player.
     */
    void addPlayer(Player player, int blocksBroken);

    /**
     * Sets the number of blocks broken for a player in the storage provider.
     *
     * @param player       The player to update.
     * @param blocksBroken The new number of blocks broken by the player.
     */
    void setPlayerCount(Player player, int blocksBroken);

    /**
     * Retrieves the number of blocks broken for a player from the storage provider.
     *
     * @param player The player to retrieve the count for.
     * @return The number of blocks broken by the player.
     */
    int getCount(Player player);

    /**
     * Retrieves the number of blocks broken for an offline player from the storage provider.
     *
     * @param player The offline player to retrieve the count for.
     * @return The number of blocks broken by the player.
     */
    int getCount(OfflinePlayer player);

    /**
     * Deletes all player data from the storage provider.
     */
    void deleteAllHoneypotPlayers();
}
