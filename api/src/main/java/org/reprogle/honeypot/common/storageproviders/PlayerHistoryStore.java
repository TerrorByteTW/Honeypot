package org.reprogle.honeypot.common.storageproviders;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface PlayerHistoryStore extends Store {
    /**
     * Add player history entry
     * @param p Player object
     * @param b Block object
     * @param action Action performed on the block
     * @param type Type of action (e.g., break, place)
     */
    void addPlayerHistory(Player p, Block b, String action, String type);

    /**
     * Retrieve player history
     * @param p Player object
     * @return List of HoneypotPlayerHistoryObject for the player
     */
    List<HoneypotPlayerHistoryObject> getPlayerHistory(Player p);

    /**
     * Delete player history entries
     * @param p Player object
     * @param n Variable number of history entry IDs to delete
     */
    void deletePlayerHistory(Player p, int... n);

    /**
     * Delete all player history entries
     */
    void deleteAllHistory();

}
