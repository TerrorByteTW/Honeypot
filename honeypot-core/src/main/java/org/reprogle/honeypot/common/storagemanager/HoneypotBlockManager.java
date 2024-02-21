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

import org.bukkit.World;
import org.bukkit.block.Block;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.storagemanager.sqlite.Database;
import org.reprogle.honeypot.common.storagemanager.sqlite.SQLite;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.util.List;

import javax.annotation.Nullable;

public class HoneypotBlockManager {

	private static HoneypotBlockManager instance = null;
	private String storageMethod = null;

	/**
	 * Returns the singleton instance of this class
	 *
	 * @return The {@link HoneypotBlockManager} instance
	 */
	public static synchronized HoneypotBlockManager getInstance() {
		if (instance == null) {
			String method = HoneypotConfigManager.getHoneypotsConfig().getString("storage-method");
			instance = new HoneypotBlockManager(method.equals("pdc") ? "pdc" : "sqlite");
		}

		return instance;
	}

	private HoneypotBlockManager(String method) {
		if (method.equalsIgnoreCase("pdc")) {
			this.storageMethod = method;
		} else {
			this.storageMethod = "sqlite";
		}
	}

	/**
	 * Create a Honeypot {@link Block} and add it to the DB
	 *
	 * @param block  The Honeypot Block we're creating
	 * @param action The action of the Honeypot
	 */
	public void createBlock(Block block, String action) {
		if (storageMethod.equals("pdc")) {
			DataStoreManager.getInstance().createHoneypotBlock(block, action);
		} else {
			Database db = new SQLite(Honeypot.plugin);
			db.load();

			db.createHoneypotBlock(block, action);
		}

		CacheManager.addToCache(new HoneypotBlockObject(block, action));
		Honeypot.getHoneypotLogger().debug("Created Honeypot block with action " + action + " at " + block.getX() + ", "
				+ block.getY() + ", " + block.getZ());
	}

	/**
	 * Delete a block from the Honeypot DB
	 *
	 * @param block The Honeypot {@link Block} we're deleting
	 */
	public void deleteBlock(Block block) {
		if (storageMethod.equals("pdc")) {
			DataStoreManager.getInstance().deleteBlock(block);
		} else {
			Database db = new SQLite(Honeypot.plugin);
			db.load();

			db.removeHoneypotBlock(block);
		}

		CacheManager.removeFromCache(new HoneypotBlockObject(block, null));
		Honeypot.getHoneypotLogger()
				.debug("Deleted Honeypot block with at " + block.getX() + ", " + block.getY() + ", " + block.getZ());
	}

	/**
	 * Check if the block is a Honeypot block
	 *
	 * @param block The {@link Block} we're checking
	 * @return true or false
	 */
	public boolean isHoneypotBlock(Block block) {
		if (CacheManager.isInCache(new HoneypotBlockObject(block, null)) != null) {
			return true;
		}

		if (storageMethod.equals("pdc")) {
			if (DataStoreManager.getInstance().isHoneypotBlock(block)) {
				String action = getAction(block);
				CacheManager.addToCache(new HoneypotBlockObject(block, action));
				return true;
			}

			return false;
		} else {
			Database db = new SQLite(Honeypot.plugin);
			db.load();

			if (db.isHoneypotBlock(block)) {
				String action = getAction(block);
				CacheManager.addToCache(new HoneypotBlockObject(block, action));
				return true;
			}

			return false;
		}

	}

	/**
	 * Get the Honeypot Block object from Cache or the DB
	 *
	 * @param block The Block to retrieve as a Honeypot Block Object
	 * @return The Honeypot Block Object if it exists, null if it doesn't
	 */
	public HoneypotBlockObject getHoneypotBlock(Block block) {

		if (Boolean.TRUE.equals(isHoneypotBlock(block)))
			return new HoneypotBlockObject(block, getAction(block));

		return null;
	}

	/**
	 * Return the action for the honeypot {@link Block}
	 *
	 * @param block The Block we're checking
	 * @return The Honeypot's action as a string
	 */
	public String getAction(Block block) {
		// Check if block exists in cache. If it doesn't this will be null
		HoneypotBlockObject potential = CacheManager.isInCache(new HoneypotBlockObject(block, null));

		if (potential != null)
			return potential.getAction();

		if (storageMethod.equals("pdc")) {
			return DataStoreManager.getInstance().getAction(block);

		} else {
			Database db = new SQLite(Honeypot.plugin);
			db.load();
			return db.getAction(block);
		}

	}

	/**
	 * Delete all Honeypots in the entire DB
	 */
	public void deleteAllHoneypotBlocks(@Nullable World world) {
		if (storageMethod.equals("pdc")) {

			DataStoreManager.getInstance().deleteAllHoneypotBlocks(world);

		} else {
			Database db = new SQLite(Honeypot.plugin);
			db.load();

			db.deleteAllBlocks();
		}

		Honeypot.getHoneypotLogger().debug("Deleted all Honeypot blocks!");
	}

	/**
	 * Get all {@link HoneypotBlockObject} in the DB
	 *
	 * @return An array list of all HoneypotBlockObjects
	 */
	public List<HoneypotBlockObject> getAllHoneypots(@Nullable World world) {
		if (storageMethod.equals("pdc")) {
			return DataStoreManager.getInstance().getAllHoneypots(world);
		} else {
			Database db = new SQLite(Honeypot.plugin);
			db.load();

			return db.getAllHoneypots();
		}
	}
}
