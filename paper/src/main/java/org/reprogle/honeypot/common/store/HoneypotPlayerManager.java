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

package org.reprogle.honeypot.common.store;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.storageproviders.PlayerStore;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

/**
 * A class for managing Players in the context of Honeypot. Does not interact with the Store, but rather uses the HoneypotRepository, as all player data
 * is stored within SQLite
 */
public class HoneypotPlayerManager {

    private final HoneypotLogger logger;

    @Inject
    public HoneypotPlayerManager(HoneypotLogger logger) {
        this.logger = logger;
    }

    /**
     * Adds a player to the database by calling the SQLite DB, setting the count for the trigger to 1, or incrementing it by `triggered` if the player already exists
     *
     * @param player      The Player object
     * @param triggerType The type of trigger that was executed
     * @param triggered   The number of triggers fired
     */
    public void addPlayer(Player player, TriggerType triggerType, int triggered) {
        Registry.getPlayerStore().addPlayer(player, triggerType, triggered);
        logger.debug(Component.text("Create Honeypot player: " + player.getName() + ", UUID of: " + player.getUniqueId()));
    }

    /**
     * Resets the count for a given trigger for a given player by calling the SQLite
     * resetPlayerCount function.
     *
     * @param player      The Player object
     * @param triggerType The type of trigger that was executed
     */
    public void resetPlayerCount(Player player, TriggerType triggerType) {
        Registry.getPlayerStore().resetPlayerCount(player, triggerType);
        logger.debug(Component.text("Reset Honeypot player: " + player.getName() + ", UUID of: " + player.getUniqueId() + " count for trigger type: " + triggerType));
    }

    /**
     * Gets the number of Honeypots the player has broken. This is NOT the total,
     * but rather the current amount until it
     * loops to 0, based on the config
     *
     * @param player      the Player object
     * @param triggerType the type of trigger that was executed
     * @return The number of Honeypot blocks the player has broken
     */
    public int getCount(Player player, TriggerType triggerType) {
        return Registry.getPlayerStore().getCount(player, triggerType);
    }

    public void playerTriggeredAction(Player player) {
        Registry.getPlayerStore().playerTriggeredAction(player);
        logger.debug(Component.text("Incremented lifetime value of actions triggered for player " + player.getName()));
    }

    /**
     * Gets the number of Honeypots the player has broken. This is NOT the total,
     * but rather the current amount until it
     * loops to 0, based on the config
     *
     * @param player      the Player name
     * @param triggerType the type of trigger that was executed
     * @return The number of Honeypot blocks the player has broken
     */
    public int getCount(OfflinePlayer player, TriggerType triggerType) {
        return Registry.getPlayerStore().getCount(player, triggerType);
    }

    /**
     * Gets the total number of Honeypots a player has ever triggered, across all trigger types. This is never reset
     *
     * @param player The player
     * @return The player's lifetime trigger count
     */
    public int getLifetimeTriggers(OfflinePlayer player) {
        return Registry.getPlayerStore().getLifetimeTriggers(player);
    }

    /**
     * Gets the total number of times an action has been run against a player
     *
     * @param player The player
     * @return The player's lifetime action count
     */
    public int getLifetimeActions(OfflinePlayer player) {
        return Registry.getPlayerStore().getLifetimeActions(player);
    }

    /**
     * Logs a warning if the given store was built for an older version of Honeypot. Such stores still work through the
     * deprecated {@link PlayerStore} methods, but only track block breaks and don't track lifetime statistics.
     *
     * @param store The store to check
     */
    public void warnIfOutdated(PlayerStore store) {
        if (!isOutdated(store)) return;

        logger.warning(Component.text("The player store \"" + store.getProviderName() + "\" was built for an older version of Honeypot. Only block breaks will be counted towards trigger limits (all other triggers take action immediately), and lifetime statistics will not be recorded. Please update the storage provider, or use Honeypot's built-in one. Support for outdated player stores will be removed in a future version of Honeypot"));
    }

    /**
     * Checks whether the current player store can count the given trigger type. Stores built for older versions of
     * Honeypot can only count {@link TriggerType#BREAK}.
     *
     * @param triggerType The trigger type to check
     * @return True if the current player store counts the trigger type
     */
    public boolean canCount(TriggerType triggerType) {
        return triggerType == TriggerType.BREAK || !isOutdated(Registry.getPlayerStore());
    }

    private static boolean isOutdated(PlayerStore store) {
        try {
            return store.getClass().getMethod("addPlayer", Player.class, TriggerType.class, int.class).getDeclaringClass() == PlayerStore.class;
        } catch (NoSuchMethodException e) {
            // Unreachable, the method is declared on PlayerStore
            return false;
        }
    }

    /**
     * Delete all players in the DB
     */
    public void deleteAllHoneypotPlayers() {
        Registry.getPlayerStore().deleteAllHoneypotPlayers();
        logger.debug(Component.text("Deleted all Honeypot players from DB"));
    }

}
