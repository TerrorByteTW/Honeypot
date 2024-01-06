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

import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.sqlite.Database;
import org.reprogle.honeypot.common.storagemanager.sqlite.SQLite;

import java.util.List;

/**
 * A class for managing Honeypot history entries.
 * Adds functions for creating, removing, querying, and purging the history
 * database.
 *
 * @see HoneypotPlayerHistoryObject
 */
public class HoneypotPlayerHistoryManager {

	private static HoneypotPlayerHistoryManager instance = null;

	private HoneypotPlayerHistoryManager() {
		// This will be made private in the next version, hence why it's deprecated
	}

	/**
	 * Returns the singleton instance of this class
	 *
	 * @return The {@link HoneypotPlayerHistoryManager} instance
	 */
	public static synchronized HoneypotPlayerHistoryManager getInstance() {
		if (instance == null)
			instance = new HoneypotPlayerHistoryManager();

		return instance;
	}

	/**
	 * Add an entry to the player history table
	 *
	 * @param p The player to add
	 * @param b The honeypot block they triggered
	 */
	public void addPlayerHistory(Player p, HoneypotBlockObject b) {
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		db.addPlayerHistory(p, b);

		Honeypot.getHoneypotLogger().debug("Added new history entry for player " + p.getName());
	}

	/**
	 * Get the history for a player
	 *
	 * @param p The player to grab history for
	 * @return A list of all HoneypotPlayerHistory objects
	 */
	public List<HoneypotPlayerHistoryObject> getPlayerHistory(Player p) {
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		return db.retrieveHistory(p);
	}

	/**
	 * Delete all history for a particular player. An optional n parameter for
	 * specifying the number of most recent rows to delete
	 *
	 * @param p The player to delete
	 * @param n Optional, the number of most recent rows
	 */
	public void deletePlayerHistory(Player p, int... n) {
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		if (n.length > 0) {
			db.deletePlayerHistory(p, n);
		} else {
			db.deletePlayerHistory(p);
		}

		Honeypot.getHoneypotLogger().debug("Deleting player history for player " + p.getName());
	}

	/**
	 * A function to purge the entire history table
	 */
	public void deleteAllHistory() {
		Database db;
		db = new SQLite(Honeypot.plugin);
		db.load();

		db.deleteAllHistory();
	}

}
