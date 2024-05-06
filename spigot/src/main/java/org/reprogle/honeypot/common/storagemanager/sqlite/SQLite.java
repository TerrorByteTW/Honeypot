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

import com.google.inject.Inject;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends Database {

	private final Honeypot plugin;
	private final HoneypotLogger logger;

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
	private static final String SQLITE_CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_players (" +
			"`playerName` VARCHAR NOT NULL," +
			"`blocksBroken` INT NOT NULL," +
			"`type` VARCHAR NOT NULL," +
			"PRIMARY KEY (`playerName`)" +
			");";

	private static final String SQLITE_CREATE_BLOCKS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_blocks (" +
			"`coordinates` VARCHAR NOT NULL," +
			"`worldName` VARCHAR NOT NULL," +
			"`action` VARCHAR NOT NULL," +
			"PRIMARY KEY (`coordinates`, `worldName`)" +
			");";

	// SQLite has this cool feature where if no primary key is provided, the primary
	// key defaults to the rowid. Nifty!
	private static final String SQLITE_CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_history (" +
			"`datetime` VARCHAR NOT NULL," +
			"`playerName` varchar NOT NULL," +
			"`playerUUID` VARCHAR NOT NULL," +
			"`coordinates` VARCHAR NOT NULL," +
			"`world` VARCHAR NOT NULL," +
			"`action` VARCHAR NOT NULL" +
			");";

	/**
	 * Get's the DB connection, also verifies if JDBC is installed. If it isn't
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
		} catch (SQLException e) {
			logger.severe("SQLException occured while attempting to create tables if they don't exist: " + e);
		}

	}

}
