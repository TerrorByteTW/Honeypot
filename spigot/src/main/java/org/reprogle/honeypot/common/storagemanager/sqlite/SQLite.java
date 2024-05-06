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

package org.reprogle.honeypot.common.storagemanager.sqlite;

import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.sqlite.patches.SQLitePatch;
import org.reprogle.honeypot.common.storagemanager.sqlite.patches.UpdatePlayerHistoryTable00;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class SQLite extends Database {

	private final Honeypot plugin;
	private final HoneypotLogger logger;

	// These two variables here are for handling patches to the database and also
	private final List<SQLitePatch> patches = new ArrayList<>(List.of(new UpdatePlayerHistoryTable00()));
	private final int DB_VERSION = 1;

	/**
	 * Create an SQLite object from the instance
	 *
	 * @param plugin The instance of the plugin
	 * @param logger The instance of the logger
	 */
	public SQLite(Honeypot plugin, HoneypotLogger logger) {
		super(plugin, logger);
		this.logger = logger;
		this.plugin = plugin;
	}

	// The queries used to load the DB table. Only runs if the table doesn't exist.
	private final String SQLITE_CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_players (" +
			"`playerName` VARCHAR NOT NULL," +
			"`blocksBroken` INT NOT NULL," +
			"`type` VARCHAR NOT NULL," +
			"PRIMARY KEY (`playerName`)" +
			");";

	private final String SQLITE_CREATE_BLOCKS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_blocks (" +
			"`coordinates` VARCHAR NOT NULL," +
			"`worldName` VARCHAR NOT NULL," +
			"`action` VARCHAR NOT NULL," +
			"PRIMARY KEY (`coordinates`, `worldName`)" +
			");";

	// SQLite has this cool feature where if no primary key is provided, the primary
	// key defaults to the rowid. Nifty!
	private final String SQLITE_CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_history (" +
			"`datetime` VARCHAR NOT NULL," +
			"`playerName` varchar NOT NULL," +
			"`playerUUID` VARCHAR NOT NULL," +
			"`coordinates` VARCHAR NOT NULL," +
			"`world` VARCHAR NOT NULL," +
			"`action` VARCHAR NOT NULL" +
			");";

	/**
	 * Gets the DB connection, also verifies if JDBC is installed. If it isn't
	 * plugin is disabled as it can't function without it
	 *
	 * @return Connection if the connection is valid, otherwise returns null
	 */
	public Connection getSQLConnection() {
		File dataFolder = new File(plugin.getDataFolder(), "honeypot.db");
		if (!dataFolder.exists()) {
			try {
				boolean success = dataFolder.createNewFile();
				if (success) {
					logger.info("Created data folder");
				} else {
					logger.severe("Could not create data folder!");
				}
			} catch (IOException e) {
				logger.severe("Could not create honeypot.db file");
			}
		}

		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
			return connection;

		} catch (SQLException e) {
			logger.severe("SQLite exception on initialize: " + e);
		} catch (ClassNotFoundException e) {
			logger.severe("SQLite JDBC Library not found. Please install this on your PC to use SQLite: " + e);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		return null;
	}

	/**
	 * Loads the DB
	 */
	@Override
	public void load() {
		connection = getSQLConnection();
		try (Statement s = connection.createStatement()) {
			s.executeUpdate(SQLITE_CREATE_PLAYERS_TABLE);
			s.executeUpdate(SQLITE_CREATE_BLOCKS_TABLE);
			s.executeUpdate(SQLITE_CREATE_HISTORY_TABLE);

			PreparedStatement ps = connection.prepareStatement("PRAGMA user_version;");
			ResultSet rs = ps.executeQuery();
			int userVersion = rs.getInt("user_version");

			//TODO How do we make this run for people updating to this version of Honeypot, while still not running when user_pragma is 0?
			// This logic only works on the very first run. It's annoying since 0 is the current version *and* default :\
			if (userVersion < DB_VERSION && userVersion != 0) {
				for (SQLitePatch patch : patches) {
					patch.update(s, logger);
				}
			} else {
				logger.debug("First time running, or DB version is higher than the version needed by the plugin. Likely the former.");
				logger.debug("Because this is the first time running, we don't need to worry about applying any patches.");
			}

			// Set the user_version pragma to 1 to prevent further patches;
			PreparedStatement pragmaStatement = connection.prepareStatement("PRAGMA user_version = ?;");
			pragmaStatement.setInt(1, DB_VERSION);
			pragmaStatement.executeUpdate();

		} catch (SQLException e) {
			logger.severe("SQLException occurred while attempting to create tables if they don't exist: " + e);
		}

	}

}
