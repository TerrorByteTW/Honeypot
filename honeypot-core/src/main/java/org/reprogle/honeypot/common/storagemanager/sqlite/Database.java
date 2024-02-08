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

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.storagemanager.queue.QueueManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a LARGE class which includes ALL the possible database operations for Honeypot. Be careful when editing!
 */
@SuppressWarnings("java:S1192")
public abstract class Database {

	Honeypot plugin;

	Connection connection;

	QueueManager qm = QueueManager.getInstance();

	private static final String PLAYER_TABLE = "honeypot_players";

	private static final String BLOCK_TABLE = "honeypot_blocks";

	private static final String HISTORY_TABLE = "honeypot_history";

	private static final String SELECT = "SELECT * FROM ";

	private static final String FAIL_TO_CLOSE = "Failed to close SQLite connection: ";

	private static final String DELETE = "DELETE FROM ";

	private static final String INSERT_INTO = "INSERT INTO ";

	private static final String WHERE = " WHERE coordinates = ? AND worldName = ?;";

	/**
	 * Create a Database object
	 *
	 * @param instance The instance of the Honeypot plugin
	 */
	protected Database(Honeypot instance) {
		plugin = instance;
	}

	public abstract Connection getSQLConnection();

	public abstract void load();

	/**
	 * Initialize the Database
	 */
	public void initialize() {
		connection = getSQLConnection();
		try (PreparedStatement ps = connection.prepareStatement(SELECT + BLOCK_TABLE)) {
			ResultSet rs = ps.executeQuery();
			close(ps, rs);
		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Unable to retrieve connection: " + e);
		}
	}

	/**
	 * Close the DB connection
	 *
	 * @param ps The PreparedStatement
	 * @param rs The ResultSet
	 */
	public void close(PreparedStatement ps, ResultSet rs) {
		try {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Failed to close SQL connection: " + e);
		}
	}

	/*****************************
	 * * BLOCK METHODS * *
	 *****************************/

	/**
	 * Create a HoneypotBlock using SQLite. Connects to the DB and inserts the block into it.
	 *
	 * @param block The Block to add to the DB
	 * @param action The action
	 */
	public void createHoneypotBlock(Block block, String action) {
		String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
		String worldName = block.getWorld().getName();

		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(INSERT_INTO + BLOCK_TABLE + " (coordinates, action, worldName) VALUES (?, ?, ?);");
			ps.setString(1, coordinates);
			ps.setString(2, action);
			ps.setString(3, worldName);
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on block table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Removes a Honeypot block from the DB. Connects to the DB and runs a DELETE FROM query
	 *
	 * @param block The block to remove
	 */
	public void removeHoneypotBlock(Block block) {
		String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
		String worldName = block.getWorld().getName();

		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(DELETE + BLOCK_TABLE + WHERE);
			ps.setString(1, coordinates);
			ps.setString(2, worldName);
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while remove executing SQL statement on block table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Checks if the DB contains the Block passed
	 *
	 * @param block The Block to check
	 * @return True if it conains the block, false if it doesn't
	 */
	@SuppressWarnings("java:S1192")
	public boolean isHoneypotBlock(Block block) {
		String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
		String worldName = block.getWorld().getName();

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(SELECT + BLOCK_TABLE + WHERE);
			ps.setString(1, coordinates);
			ps.setString(2, worldName);
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getString("coordinates").equalsIgnoreCase(coordinates)) {
					return true;
				}
			}
		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing check SQL statement on block table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
				if (rs != null)
					rs.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}

		return false;
	}

	/**
	 * Gets the action of the Honeypot block passes
	 *
	 * @param block The Honeypot block to check
	 * @return The action in String form
	 */
	@SuppressWarnings("java:S1192")
	public String getAction(Block block) {
		String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
		String worldName = block.getWorld().getName();

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(SELECT + BLOCK_TABLE + WHERE);
			ps.setString(1, coordinates);
			ps.setString(2, worldName);
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getString("coordinates").equalsIgnoreCase(coordinates)) {
					return rs.getString("action");
				}
			}
		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing action SQL statement on block table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
				if (rs != null)
					rs.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}

		return null;
	}

	/**
	 * Get all the Honeypot blocks in the DB
	 *
	 * @return A list of HoneypotBlockObjects from the DB
	 */
	public List<HoneypotBlockObject> getAllHoneypots() {
		ArrayList<HoneypotBlockObject> blocks = new ArrayList<>();
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(SELECT + BLOCK_TABLE + ";");
			rs = ps.executeQuery();

			while (rs.next()) {
				blocks.add(new HoneypotBlockObject(rs.getString("worldName"), rs.getString("coordinates"),
						rs.getString("action")));
			}

			return blocks;
		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing action SQL statement on block table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
				if (rs != null)
					rs.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}

		return Collections.emptyList();
	}

	/*****************************
	 * * PLAYER METHODS * *
	 *****************************/

	/**
	 * Creates a Honeypot Player
	 *
	 * @param player The Player to create
	 * @param blocksBroken The number of Blocks the player has broken
	 */
	@SuppressWarnings("java:S1192")
	public void createHoneypotPlayer(Player player, int blocksBroken) {

		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(INSERT_INTO + PLAYER_TABLE + " (playerName, blocksBroken) VALUES (?, ?);");
			ps.setString(1, player.getUniqueId().toString());
			ps.setInt(2, blocksBroken);
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Sets the player count
	 *
	 * @param playerName The Player object of the Playe to set
	 * @param blocksBroken The number of Blocks broken
	 */
	public void setPlayerCount(Player playerName, int blocksBroken) {

		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement("REPLACE INTO " + PLAYER_TABLE + " (playerName, blocksBroken) VALUES (?, ?);");
			ps.setString(1, playerName.getUniqueId().toString());
			ps.setInt(2, blocksBroken);
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger()
					.severe("Error while executing count update SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Get's the current number of block the player has broken
	 *
	 * @param player The Player to check
	 * @return An int representing how many blocks the player has broken
	 */
	public int getCount(Player player) {

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(SELECT + PLAYER_TABLE + " WHERE playerName = ?;");
			ps.setString(1, player.getUniqueId().toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getString("playerName").equalsIgnoreCase(player.getUniqueId().toString())) {
					return rs.getInt("blocksBroken");
				}
			}
		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger()
					.severe("Error while executing count retrieval SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
				if (rs != null)
					rs.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}

		return -1;
	}

	/**
	 * Get's the current number of block the player has broken
	 *
	 * @param player The OfflinePlayer to check
	 * @return An int representing how many blocks the player has broken. Returns -1 if the player doesn't exist
	 */
	public int getCount(OfflinePlayer player) {

		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(SELECT + PLAYER_TABLE + " WHERE playerName = ?;");
			ps.setString(1, player.getUniqueId().toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getString("playerName").equalsIgnoreCase(player.getUniqueId().toString())) {
					return rs.getInt("blocksBroken");
				}
			}
		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger()
					.severe("Error while executing count retrieval SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
				if (rs != null)
					rs.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}

		return -1;
	}

	/*****************************
	 * * DROP METHODS * *
	 *****************************/

	/**
	 * Delete's all blocks in the DB
	 */
	public void deleteAllBlocks() {
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(DELETE + BLOCK_TABLE + ";");
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Delete's all players from the DB
	 */
	public void deleteAllPlayers() {
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(DELETE + PLAYER_TABLE + ";");
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Delete all player history
	 */
	public void deleteAllHistory() {
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(DELETE + HISTORY_TABLE + ";");
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on player table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/*****************************
	 * * HISTORY METHODS * *
	 *****************************/

	/**
	 * Add a player to the history database
	 *
	 * @param p The player to add
	 * @param block The HoneypotBlock to add
	 */
	public void addPlayerHistory(Player p, HoneypotBlockObject block) {
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			ps = c.prepareStatement(INSERT_INTO + HISTORY_TABLE
					+ " (datetime, playerName, playerUUID, coordinates, world, action) VALUES (DATETIME('now'), ?, ?, ?, ?, ?);");
			ps.setString(1, p.getName());
			ps.setString(2, p.getUniqueId().toString());
			ps.setString(3, block.getCoordinates());
			ps.setString(4, block.getWorld());
			ps.setString(5, block.getAction());
			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on history table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}

	/**
	 * Retrieve all history for a player
	 *
	 * @param p The Player to retrieve
	 * @return An ArrayList of HoneypotPlayerHistory objects for the player
	 */
	public List<HoneypotPlayerHistoryObject> retrieveHistory(Player p) {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs;
		try {
			c = getSQLConnection();
			ps = c.prepareStatement(SELECT + HISTORY_TABLE + " WHERE playerUUID = ?;");
			ps.setString(1, p.getUniqueId().toString());
			rs = ps.executeQuery();

			List<HoneypotPlayerHistoryObject> history = new ArrayList<>();
			while (rs.next()) {
				HoneypotBlockObject hbo = new HoneypotBlockObject(rs.getString("world"), rs.getString("coordinates"),
						rs.getString("action"));
				history.add(new HoneypotPlayerHistoryObject(rs.getString("datetime"), rs.getString("playerName"),
						rs.getString("playerUUID"), hbo));
			}

			return history;

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing create SQL statement on history table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Delete a player's most recent history. An optional 'n' value is listed to allow for deleting a certain number of
	 * rows
	 *
	 * @param p The player to delete history for
	 * @param n An optional int, representing the number of most recent items to delete. An array may be supplied here,
	 * but only index 0 will be used
	 */
	public void deletePlayerHistory(Player p, int... n) {
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = getSQLConnection();
			if (n.length > 0) {
				ps = c.prepareStatement(DELETE + HISTORY_TABLE + " WHERE rowid IN (SELECT rowid FROM " + HISTORY_TABLE
						+ " WHERE playerUUID = ? ORDER BY rowid DESC LIMIT ?);");
				ps.setString(1, p.getUniqueId().toString());
				ps.setInt(2, n[0]);
			}
			else {
				ps = c.prepareStatement(DELETE + HISTORY_TABLE + " WHERE playerUUID = ?;");
				ps.setString(1, p.getUniqueId().toString());
			}

			qm.addToQueue(ps);

		}
		catch (SQLException e) {
			Honeypot.getHoneypotLogger().severe("Error while executing SQL statement on block table: " + e);
		}
		finally {
			try {
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			}
			catch (SQLException e) {
				Honeypot.getHoneypotLogger().severe(FAIL_TO_CLOSE + e);
			}
		}
	}
}
