/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
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
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.TriggerType;
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
        try {
            Registry.getPlayerStore().addPlayer(player, triggerType, triggered);
        } catch (NotImplementedException e) {
            logger.debug(Component.text("The current Storage Provider is outdated and is using the deprecated implementation of addPlayer(). Please update your storage provider, or use Honeypot's built-in ones. It will stop working in the next version of Honeypot"));
            Registry.getPlayerStore().addPlayer(player, triggered);
        }
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
        try {
            return Registry.getPlayerStore().getCount(player, triggerType);
        } catch (NotImplementedException e) {
            logger.debug(Component.text("The current Storage Provider is outdated and is using the deprecated implementation of getCount(). Please update your storage provider, or use Honeypot's built-in ones. It will stop working in the next version of Honeypot"));
            return Registry.getPlayerStore().getCount(player);
        }
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
        try {
            return Registry.getPlayerStore().getCount(player, triggerType);
        } catch (NotImplementedException e) {
            logger.debug(Component.text("The current Storage Provider is outdated and is using the deprecated implementation of getCount(). Please update your storage provider, or use Honeypot's built-in ones. It will stop working in the next version of Honeypot"));
            return Registry.getPlayerStore().getCount(player);
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
