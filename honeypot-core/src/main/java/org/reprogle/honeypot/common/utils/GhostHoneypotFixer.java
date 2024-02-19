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

package org.reprogle.honeypot.common.utils;

import org.bukkit.Material;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.folia.Scheduler;

import java.util.List;

@SuppressWarnings({ "java:S1604" })
public class GhostHoneypotFixer {

	// Create package constructor to hide implicit one
	public GhostHoneypotFixer() {
		// Start the GhostHoneypotFixer
		if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("ghost-honeypot-checker.enable"))) {
			Honeypot.getHoneypotLogger().info(
					"Starting the ghost checker task! If you need to change the settings for this function, edit the config then do /honeypot reload");
			this.startTask();
		}
	}

	private Scheduler.ScheduledTask task;

	/**
	 * Start a task to check for ghost honeypots every defined interval
	 */
	public void startTask() {
		// Store the task, just in case we need it in the future. For now, it's unused
		// so we're silencing unused warnings

		task = Scheduler.runTaskTimer(Honeypot.plugin, () -> {
			Honeypot.getHoneypotLogger().info("Running ghost Honeypot checks...");
			int removedPots = 0;
			List<HoneypotBlockObject> pots = HoneypotBlockManager.getInstance().getAllHoneypots();
			for (HoneypotBlockObject pot : pots) {

				Material block;

				/*
				 * This try/catch stems from Folia, where a region may not be ticking yet, or
				 * even loaded, so the
				 * getType() method returns an error. Remember, `pot` is a HoneypotBlockObject,
				 * not a Block itself, so
				 * #getBlock() may return null (Usually not, but it's possible) This is a place
				 * we can improve, but for
				 * now it's fine since I've never seen the error before prior to testing Folia.
				 * Don't get me wrong, I
				 * hate the mindset of "I haven't seen it break so it obviously won't" when
				 * *clearly* the Spigot API
				 * docs state that it *can* break, but I'm going to put a pin in it for now :)
				 */
				try {
					block = pot.getBlock().getType();
				} catch (NullPointerException e) {
					Honeypot.getHoneypotLogger().warning("Could not get the material for Honeypot at "
							+ pot.getCoordinates() + " because the world isn't loaded yet (Maybe running Folia?)");
					continue;
				}

				/*
				 * Check for water and lava in the event that BlockFromToEvent has been disabled
				 * in config, meaning that water and lava may end up occupying Honeypot spaces
				 * in some instances (Such as if a Honeypot was set as a torch)
				 */
				if (block.equals(Material.AIR) || block.equals(Material.WATER) || block.equals(Material.LAVA)) {
					Honeypot.getHoneypotLogger()
							.debug("Found ghost Honeypot at " + pot.getCoordinates() + ". Removing");
					HoneypotBlockManager.getInstance().deleteBlock(pot.getBlock());
					removedPots++;
				}
			}

			Honeypot.getHoneypotLogger()
					.info("Finished ghost Honeypot checks! Removed " + removedPots + " ghost Honeypots.");
		}, 0L, 20L * 60 * HoneypotConfigManager.getPluginConfig().getInt("ghost-honeypot-checker.check-interval"));
	}

	/**
	 * Cancel the ghost honeypot task
	 */
	public void cancelTask() {
		task.cancel();
	}

}
