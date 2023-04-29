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

package org.reprogle.honeypot.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;

import java.util.List;

@SuppressWarnings({"java:S1604"})
public class GhostHoneypotFixer {

	// Create package constructor to hide implicit one
	public GhostHoneypotFixer() {
		// Start the GhostHoneypotFixer
		if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("ghost-honeypot-checker.enable"))) {
			Honeypot.plugin.getLogger().info(
					"Starting the ghost checker task! If you need to disable this, update the config and restart the server");
			this.startTask();
		}
	}

	private static BukkitTask task;

	/**
	 * Start a task to check for ghost honeypots every defined interval
	 */
	public void startTask() {
		// Store the task, just in case we need it in the future. For now, it's unused
		// so we're silencing unused warnings
		task = Bukkit.getScheduler().runTaskTimer(Honeypot.plugin, () -> {
			Honeypot.plugin.getLogger().info("Running ghost Honeypot checks...");
			Honeypot.getHoneypotLogger().log("Running ghost Honeypot checks...");
			int removedPots = 0;
			List<HoneypotBlockObject> pots = HoneypotBlockManager.getInstance().getAllHoneypots();
			for (HoneypotBlockObject pot : pots) {
				if (pot.getBlock().getType().equals(Material.AIR)) {
					Honeypot.plugin.getLogger()
							.info("Found ghost Honeypot at " + pot.getCoordinates() + ". Removing");
					Honeypot.getHoneypotLogger()
							.log("Found ghost Honeypot at " + pot.getCoordinates() + ". Removing");
					HoneypotBlockManager.getInstance().deleteBlock(pot.getBlock());
					removedPots++;
				}
			}

			Honeypot.plugin.getLogger()
					.info("Finished ghost Honeypot checks! Removed " + removedPots + " ghost Honeypots.");
			Honeypot.getHoneypotLogger()
					.log("Finished ghost Honeypot checks! Removed " + removedPots + " ghost Honeypots.");
		}, 0L, 20L * 60 * HoneypotConfigManager.getPluginConfig().getInt("ghost-honeypot-checker.check-interval"));
	}

	/**
	 * Cancel the ghost honeypot task
	 */
	public void cancelTask() {
		task.cancel();
	}

}
