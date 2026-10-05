/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022-2026 TerrorByte and Honeypot Contributors.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storageproviders;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

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
     * Add player history entry, recording {@code blockType} as the block's type rather than the block's current type.
     * This is used when the Honeypot block has already been destroyed by the time it's logged, such as a torch that
     * broke because the block it was attached to was broken.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and records the
     * block's current type.
     *
     * @param p         Player object
     * @param b         Block object
     * @param blockType The type of the block when it was triggered
     * @param action    Action performed on the block
     * @param type      Type of action (e.g., break, place)
     */
    default void addPlayerHistory(Player p, Block b, Material blockType, String action, String type) {
        addPlayerHistory(p, b, action, type);
    }

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
     * Retrieve a single page of history, newest first, for a player who may be offline. Stores should override this
     * with a native query by UUID.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and can only
     * look up players who are currently online.
     *
     * @param uuid   UUID of the player
     * @param offset Number of entries to skip
     * @param limit  Maximum number of entries to return
     * @return List of HoneypotPlayerHistoryObject for the player within the requested range
     * @throws UnsupportedOperationException if the player is offline and this store doesn't support offline lookups
     */
    default List<HoneypotPlayerHistoryObject> getPlayerHistory(UUID uuid, int offset, int limit) {
        return getPlayerHistory(requireOnline(uuid), offset, limit);
    }

    /**
     * Count the number of history entries for a player who may be offline. Stores should override this with a
     * native count query by UUID.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and can only
     * look up players who are currently online.
     *
     * @param uuid UUID of the player
     * @return The number of history entries for the player
     * @throws UnsupportedOperationException if the player is offline and this store doesn't support offline lookups
     */
    default int getPlayerHistoryCount(UUID uuid) {
        return getPlayerHistoryCount(requireOnline(uuid));
    }

    /**
     * Delete history entries for a player who may be offline. Stores should override this with a native delete by
     * UUID.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and can only
     * delete history for players who are currently online.
     *
     * @param uuid UUID of the player
     * @param n    Optional, the number of most recent entries to delete
     * @throws UnsupportedOperationException if the player is offline and this store doesn't support offline lookups
     */
    default void deletePlayerHistory(UUID uuid, int... n) {
        deletePlayerHistory(requireOnline(uuid), n);
    }

    /**
     * Delete all player history entries
     */
    void deleteAllHistory();

    private Player requireOnline(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            throw new UnsupportedOperationException("Storage provider " + getClass().getName() + " does not support history lookups for offline players");
        return player;
    }

}
