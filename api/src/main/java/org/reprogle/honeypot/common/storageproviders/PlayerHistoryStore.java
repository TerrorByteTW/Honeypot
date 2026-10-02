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
     * Retrieve a single page of player history, newest first. Stores should override this with a native
     * paginated query; the default implementation loads the entire history and slices it.
     * @param p Player object
     * @param offset Number of entries to skip
     * @param limit Maximum number of entries to return
     * @return List of HoneypotPlayerHistoryObject for the player within the requested range
     */
    default List<HoneypotPlayerHistoryObject> getPlayerHistory(Player p, int offset, int limit) {
        List<HoneypotPlayerHistoryObject> history = getPlayerHistory(p);
        if (offset >= history.size()) return List.of();
        return history.subList(Math.max(offset, 0), Math.min(offset + limit, history.size()));
    }

    /**
     * Count the number of history entries for a player. Stores should override this with a native
     * count query; the default implementation loads the entire history.
     * @param p Player object
     * @return The number of history entries for the player
     */
    default int getPlayerHistoryCount(Player p) {
        return getPlayerHistory(p).size();
    }

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
