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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

/**
 * A class for managing Players in the context of Honeypot. Does not interact with the Store, but rather uses the HoneypotRepository, as all player data
 * is stored within SQLite (For now)
 */
public class HoneypotPlayerManager {

	private final HoneypotLogger logger;

	@Inject
	public HoneypotPlayerManager(HoneypotLogger logger) {
		this.logger = logger;
	}

	/**
	 * Create a honeypot block by calling the SQLite DB. In the future this will be
	 * a switch case statement to handle
	 * multiple DB types
	 *
	 * @param player       The Player object
	 * @param blocksBroken The amount of Blocks broken
	 */
	public void addPlayer(Player player, int blocksBroken) {
		Registry.getStorageProvider().addPlayer(player, blocksBroken);
		logger.debug(Component.text("Create Honeypot player: " + player.getName() + ", UUID of: " + player.getUniqueId()), true);
	}

	/**
	 * Set the number of blocks broken by the player by calling the SQLite
	 * setPlayerCount function. In the future this
	 * will be a switch case statement to handle multiple DB types without changing
	 * code
	 *
	 * @param player       The Player object
	 * @param blocksBroken The amount of blocks broken by the player
	 */
	public void setPlayerCount(Player player, int blocksBroken) {
		Registry.getStorageProvider().setPlayerCount(player, blocksBroken);
		logger.debug(Component.text("Updated Honeypot player: " + player.getName() + ", UUID of: " + player.getUniqueId() + ". New count: " + blocksBroken), true);
	}

	/**
	 * Gets the amount of Honeypots the player has broken. This is NOT the total,
	 * but rather the current amount until it
	 * loops to 0, based on the config
	 *
	 * @param player the Player object
	 * @return The amount of Honeypot blocks the player has broken
	 */
	public int getCount(Player player) {
		return Registry.getStorageProvider().getCount(player);
	}

	/**
	 * Gets the amount of Honeypots the player has broken. This is NOT the total,
	 * but rather the current amount until it
	 * loops to 0, based on the config
	 *
	 * @param player the Player name
	 * @return The amount of Honeypot blocks the player has broken
	 */
	public int getCount(OfflinePlayer player) {
		return Registry.getStorageProvider().getCount(player);
	}

	/**
	 * Delete's all players in the DB
	 */
	public void deleteAllHoneypotPlayers() {
		Registry.getStorageProvider().deleteAllHoneypotPlayers();
		logger.debug(Component.text("Deleted all Honeypot players from DB"), false);
	}

}
