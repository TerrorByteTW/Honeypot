package org.reprogle.honeypot.common.storageproviders;

import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.api.events.TriggerType;

import javax.naming.OperationNotSupportedException;
import java.util.HashMap;

public interface PlayerStore extends Store {
    /**
     * Adds a player to the storage provider with the specified number of blocks broken.
     *
     * @param player       The player to add.
     * @param blocksBroken The number of blocks broken by the player.
     * @deprecated Storage providers should no longer implement this and should instead use the `addPlayer()` method that uses `TriggerType` instead.
     */
    @Deprecated
    default void addPlayer(Player player, int blocksBroken) {
        throw new NotImplementedException("This storage provider is built for a newer version of Honeypot and will not work here! Please update your version of Honeypot");
    }

    /**
     * Adds or updates a player to the storage provider with the specified trigger and number of triggers
     *
     * @param player      The player to add.
     * @param triggerType The type of trigger to associate with the player.
     * @param triggered   The new number of Honeypots triggered by the player.
     */
    default void addPlayer(Player player, TriggerType triggerType, int triggered) {
        throw new NotImplementedException("This storage provider needs updated to support trigger counts! This is NOT a bug!");
    }

    /**
     * Adds or updates a player's lifetime trigger count
     *
     * @param player The player to add or update
     */
    default void playerTriggeredAction(Player player) {
        throw new NotImplementedException("This storage provider needs updated to support trigger counts! This is NOT a bug!");
    }

    /**
     * Sets the number of blocks broken for a player in the storage provider.
     *
     * @param player       The player to update.
     * @param blocksBroken The new number of blocks broken by the player.
     * @deprecated Storage providers should no longer implement this and should instead use the `addPlayer()` method that uses `TriggerType` instead, which will insert OR update as necessary.
     */
    @Deprecated
    default void setPlayerCount(Player player, int blocksBroken) {
        throw new NotImplementedException("This storage provider is built for a newer version of Honeypot and will not work here! Please update your version of Honeypot");
    }

    /**
     * Resets a trigger count for a player to 0
     *
     * @param player  The player to reset
     * @param trigger The trigger type to reset
     */
    default void resetPlayerCount(Player player, TriggerType trigger) {
        throw new NotImplementedException("This storage provider needs updated to support trigger counts! This is NOT a bug!");
    }

    /**
     * Retrieves the number of blocks broken for a player from the storage provider.
     *
     * @param player The player to retrieve the count for.
     * @return The number of blocks broken by the player.
     * @deprecated Storage providers should no longer implement this and should instead use the `getCount()` method that uses `TriggerType` instead.
     */
    @Deprecated
    default int getCount(Player player) {
        throw new NotImplementedException("This storage provider is built for a newer version of Honeypot and will not work here! Please update your version of Honeypot");
    }

    /**
     * Gets the number of Honeypots triggered for a given trigger type
     *
     * @param trigger The trigger type to get the count for, or 0 if not found
     * @return The count for the specified trigger type
     */
    default int getCount(Player player, TriggerType trigger) {
        throw new NotImplementedException("This storage provider needs updated to support trigger counts! This is NOT a bug!");
    }

    /**
     * Retrieves the number of blocks broken for an offline player from the storage provider.
     *
     * @param player The offline player to retrieve the count for.
     * @return The number of blocks broken by the player.
     * @deprecated Storage providers should no longer implement this and should instead use the `getCount()` method that uses `TriggerType` instead.
     */
    @Deprecated
    default int getCount(OfflinePlayer player) {
        throw new NotImplementedException("This storage provider is built for a newer version of Honeypot and will not work here! Please update your version of Honeypot");
    }

    /**
     * Gets the number of Honeypots triggered for a given trigger type
     *
     * @param trigger The trigger type to get the count for, or 0 if not found
     * @return The count for the specified trigger type
     */
    default int getCount(OfflinePlayer player, TriggerType trigger) {
        throw new NotImplementedException("This storage provider needs updated to support trigger counts! This is NOT a bug!");
    }

    /**
     * Deletes all player data from the storage provider.
     */
    void deleteAllHoneypotPlayers();

    /**
     * Gets the hashmap of all trigger counts
     *
     * @param player The player to retrieve the trigger counts for
     * @return The hashmap of all trigger counts for the player
     */
    default HashMap<TriggerType, Integer> getTriggerCounts(Player player) {
        throw new NotImplementedException("This storage provider needs updated to support trigger counts! This is NOT a bug!");
    }
}
