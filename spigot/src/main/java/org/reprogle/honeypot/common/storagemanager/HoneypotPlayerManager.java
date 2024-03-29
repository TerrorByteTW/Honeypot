/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storagemanager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.sqlite.Database;
import org.reprogle.honeypot.common.storagemanager.sqlite.SQLite;

public class HoneypotPlayerManager {

	private static HoneypotPlayerManager instance = null;

	/**
	 * Returns the singleton instance of this class
	 *
	 * @return The {@link HoneypotPlayerManager} instance
	 */
	public static synchronized HoneypotPlayerManager getInstance() {
		if (instance == null)
			instance = new HoneypotPlayerManager();

		return instance;
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
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		db.createHoneypotPlayer(player, blocksBroken);
		Honeypot.getHoneypotLogger()
				.info("Create Honeypot player: " + player.getName() + ", UUID of: " + player.getUniqueId());
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
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		db.setPlayerCount(player, blocksBroken);
		Honeypot.getHoneypotLogger().debug("Updated Honeypot player: " + player.getName() + ", UUID of: "
				+ player.getUniqueId() + ". New count: " + blocksBroken);
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
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		return db.getCount(player);
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
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		return db.getCount(player);
	}

	/**
	 * Delete's all players in the DB
	 */
	public void deleteAllHoneypotPlayers() {
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		db.deleteAllPlayers();
		Honeypot.getHoneypotLogger().debug("Deleted all Honeypot players from DB");
	}

}
