/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.api.events.TriggerType;

import java.util.HashMap;

/**
 * A storage provider for Honeypot player data.
 * <p>
 * Providers built for older versions of Honeypot only implement the deprecated, block-break-only methods. The
 * {@link TriggerType} methods default to translating onto those, so legacy providers keep working for
 * {@link TriggerType#BREAK} triggers while ignoring every other trigger type. Up-to-date providers should override every
 * non-deprecated method and leave the deprecated ones alone.
 */
public interface PlayerStore extends Store {
    /**
     * Adds a player to the storage provider with the specified number of blocks broken.
     *
     * @param player       The player to add.
     * @param blocksBroken The number of blocks broken by the player.
     * @deprecated Storage providers should no longer implement this and should instead implement {@link #addPlayer(Player, TriggerType, int)}.
     */
    @Deprecated
    default void addPlayer(Player player, int blocksBroken) {
        throw new UnsupportedOperationException("This storage provider does not implement addPlayer(Player, TriggerType, int). Please update your storage provider");
    }

    /**
     * Adds a player to the storage provider if they don't exist, and increments the count for the given trigger type by
     * {@code triggered}. Also increments the player's lifetime trigger count by {@code triggered}.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and only tracks
     * {@link TriggerType#BREAK}.
     *
     * @param player      The player to add or update.
     * @param triggerType The type of trigger to increment.
     * @param triggered   The number of triggers to add to the player's count.
     */
    default void addPlayer(Player player, TriggerType triggerType, int triggered) {
        // Legacy providers only have a single counter, which tracks block breaks
        if (triggerType != TriggerType.BREAK) return;

        int current = getCount(player);
        if (current < 0) {
            addPlayer(player, triggered);
        } else {
            setPlayerCount(player, current + triggered);
        }
    }

    /**
     * Increments a player's lifetime count of actions taken against them.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, which don't track
     * lifetime statistics, and does nothing.
     *
     * @param player The player to update
     */
    default void playerTriggeredAction(Player player) {
        // Legacy providers don't track lifetime statistics
    }

    /**
     * Gets the total number of Honeypots a player has ever triggered, across all trigger types. Unlike
     * {@link #getCount(OfflinePlayer, TriggerType)}, this is never reset.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, which don't track
     * lifetime statistics, and returns 0.
     *
     * @param player The player to retrieve the total for
     * @return The player's lifetime trigger count, or 0 if the player doesn't exist
     */
    default int getLifetimeTriggers(OfflinePlayer player) {
        return 0;
    }

    /**
     * Gets the total number of times an action has been run against a player.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, which don't track
     * lifetime statistics, and returns 0.
     *
     * @param player The player to retrieve the total for
     * @return The player's lifetime action count, or 0 if the player doesn't exist
     */
    default int getLifetimeActions(OfflinePlayer player) {
        return 0;
    }

    /**
     * Sets the number of blocks broken for a player in the storage provider.
     *
     * @param player       The player to update.
     * @param blocksBroken The new number of blocks broken by the player.
     * @deprecated Storage providers should no longer implement this and should instead implement {@link #addPlayer(Player, TriggerType, int)} and {@link #resetPlayerCount(Player, TriggerType)}.
     */
    @Deprecated
    default void setPlayerCount(Player player, int blocksBroken) {
        throw new UnsupportedOperationException("This storage provider does not implement resetPlayerCount(Player, TriggerType). Please update your storage provider");
    }

    /**
     * Resets a trigger count for a player to 0
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and only tracks
     * {@link TriggerType#BREAK}.
     *
     * @param player  The player to reset
     * @param trigger The trigger type to reset
     */
    default void resetPlayerCount(Player player, TriggerType trigger) {
        if (trigger == TriggerType.BREAK) setPlayerCount(player, 0);
    }

    /**
     * Retrieves the number of blocks broken for a player from the storage provider.
     *
     * @param player The player to retrieve the count for.
     * @return The number of blocks broken by the player, or -1 if the player doesn't exist.
     * @deprecated Storage providers should no longer implement this and should instead implement {@link #getCount(Player, TriggerType)}.
     */
    @Deprecated
    default int getCount(Player player) {
        throw new UnsupportedOperationException("This storage provider does not implement getCount(Player, TriggerType). Please update your storage provider");
    }

    /**
     * Gets the number of Honeypots triggered by a player for a given trigger type.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and only tracks
     * {@link TriggerType#BREAK}.
     *
     * @param player  The player to retrieve the count for
     * @param trigger The trigger type to get the count for
     * @return The count for the specified trigger type, or 0 if the player doesn't exist
     */
    default int getCount(Player player, TriggerType trigger) {
        return trigger == TriggerType.BREAK ? Math.max(getCount(player), 0) : 0;
    }

    /**
     * Retrieves the number of blocks broken for an offline player from the storage provider.
     *
     * @param player The offline player to retrieve the count for.
     * @return The number of blocks broken by the player, or -1 if the player doesn't exist.
     * @deprecated Storage providers should no longer implement this and should instead implement {@link #getCount(OfflinePlayer, TriggerType)}.
     */
    @Deprecated
    default int getCount(OfflinePlayer player) {
        throw new UnsupportedOperationException("This storage provider does not implement getCount(OfflinePlayer, TriggerType). Please update your storage provider");
    }

    /**
     * Gets the number of Honeypots triggered by an offline player for a given trigger type.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and only tracks
     * {@link TriggerType#BREAK}.
     *
     * @param player  The offline player to retrieve the count for
     * @param trigger The trigger type to get the count for
     * @return The count for the specified trigger type, or 0 if the player doesn't exist
     */
    default int getCount(OfflinePlayer player, TriggerType trigger) {
        return trigger == TriggerType.BREAK ? Math.max(getCount(player), 0) : 0;
    }

    /**
     * Deletes all player data from the storage provider.
     */
    void deleteAllHoneypotPlayers();

    /**
     * Gets the counts for every trigger type a player has a count for.
     * <p>
     * The default implementation exists only for providers built for older versions of Honeypot, and only tracks
     * {@link TriggerType#BREAK}.
     *
     * @param player The player to retrieve the trigger counts for
     * @return A map of trigger types to the player's current count for each
     */
    default HashMap<TriggerType, Integer> getTriggerCounts(Player player) {
        HashMap<TriggerType, Integer> counts = new HashMap<>();
        counts.put(TriggerType.BREAK, getCount(player, TriggerType.BREAK));
        return counts;
    }
}
