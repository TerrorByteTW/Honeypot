/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.sqlite.patches.SQLitePatch;
import org.reprogle.honeypot.common.storagemanager.sqlite.patches.UpdateHistoryTable00;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
@Singleton
public class SQLite extends Database {

	private final Honeypot plugin;
	private final HoneypotLogger logger;

	// These two variables here are for handling patches to the database and also
	private final List<SQLitePatch> patches = new ArrayList<>(List.of(new UpdateHistoryTable00()));
	private final int DB_VERSION = 1;

	/**
	 * Create an SQLite object from the instance
	 *
	 * @param plugin The instance of the plugin
	 * @param logger The instance of the logger
	 */
	@Inject
	public SQLite(Honeypot plugin, HoneypotLogger logger) {
		super(plugin, logger);
		this.logger = logger;
		this.plugin = plugin;

		connection = getSQLConnection();
		try (Statement s = connection.createStatement()) {
			// Get the user_version of the database
			PreparedStatement ps = connection.prepareStatement("PRAGMA user_version;");
			ResultSet rs = ps.executeQuery();
			int userVersion = rs.getInt("user_version");

			// Check if the DB needs an upgrade
			logger.debug(Component.text("Checking if DB needs upgrading"));
			boolean upgradeNecessary = checkIfUpgradeNecessary(connection, userVersion);

			// If the plugin is being run for the first time
			if (!upgradeNecessary) {
				logger.debug(Component.text("No upgrade necessary, first run or DB schema is up to date. Creating tables if they don't exist, otherwise skipping"));
				s.executeUpdate(SQLITE_CREATE_PLAYERS_TABLE);
				s.executeUpdate(SQLITE_CREATE_BLOCKS_TABLE);
				s.executeUpdate(SQLITE_CREATE_HISTORY_TABLE);
			} else {
				logger.debug(Component.text("It appears the plugin DB needs patched, userVersion is " + userVersion + " and the current version is " + DB_VERSION + ". Let's apply patches now"));

				for (SQLitePatch patch : patches) {
					// Only apply the patch if the current version of the DB is less than the version of the DB patch
					if (userVersion < patch.patchedIn()) {
						// Apply the patch
						logger.debug(Component.text("Applying patch '" + patch.getClass().getName() + "'"));
						patch.update(s, logger);
					} else {
						logger.debug(Component.text("Patch '" + patch.getClass().getName() + "' version is " + patch.patchedIn() + " while the DB_VERSION is " + DB_VERSION + ". Skipping since this patch is not needed"));
					}
				}

				logger.debug(Component.text("Finished applying patches"));
			}

			// Set the user_version pragma to DB_VERSION to prevent further patches
			s.executeUpdate(SET_PRAGMA);
		} catch (SQLException e) {
			logger.severe(Component.text("SQLException occurred while creating SQLite connection: " + e.getMessage()));
			logger.severe(Component.text("Full stack" + Arrays.toString(e.getStackTrace())));
		}
	}

	// The queries used to load the DB table. Only runs if the table doesn't exist.
	private final String SQLITE_CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_players (" +
			"`playerName` VARCHAR NOT NULL," +
			"`blocksBroken` INT NOT NULL," +
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
			"`type` VARCHAR NOT NULL," +
			"`action` VARCHAR NOT NULL" +
			");";

	private final String SET_PRAGMA = "PRAGMA user_version = " + DB_VERSION + ";";

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
					logger.info(Component.text("Created data folder"));
				} else {
					logger.severe(Component.text("Could not create data folder!"));
				}
			} catch (IOException e) {
				logger.severe(Component.text("Could not create honeypot.db file"));
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
			logger.severe(Component.text("SQLite exception on initialize: " + e));
		} catch (ClassNotFoundException e) {
			logger.severe(Component.text("SQLite JDBC Library not found. Please install this on your PC to use SQLite: " + e));
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		return null;
	}

	/**
	 * Here we check if there are any tables in the DB. If there are, and checkIfInitialRun is false, then we know an upgrade is necessary.
	 * @param connection The connection of the DB
	 * @param userVersion The current version of the DB on disk
	 * @return True if an upgrade is necessary
	 */
	public boolean checkIfUpgradeNecessary(Connection connection, int userVersion) {
		boolean alreadyInitialized;
		boolean tablesExist;

		alreadyInitialized = userVersion >= DB_VERSION;

		// Then we check if any tables exist at all in the DB
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';");
			ResultSet rs = ps.executeQuery();
			tablesExist = rs.next();
		} catch (SQLException e) {
			tablesExist = false;
		}

		return (!alreadyInitialized && tablesExist);
	}

}
