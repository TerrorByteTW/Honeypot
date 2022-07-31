package org.reprogle.honeypot.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;

@SuppressWarnings({ "java:S1604", "unused" })
public class GhostHoneypotFixer {

	// Create package constructor to hide implicit one
	GhostHoneypotFixer() {

	}

	public static BukkitTask task;

	/**
	 * Start a task to check for ghost honeypots every defined interval
	 */
	public static void startTask() {
		// Store the task, just in case we need it in the future. For now, it's unused so we're silencing unused warnings
		task = Bukkit.getScheduler().runTaskTimer(Honeypot.getPlugin(), new Runnable() {

			@Override
			public void run() {
				Honeypot.getPlugin().getLogger().info("Running ghost Honeypot checks...");
				Honeypot.getHoneypotLogger().log("Running ghost Honeypot checks...");
				int removedPots = 0;
				List<HoneypotBlockObject> pots = Honeypot.getHBM().getAllHoneypots();	
				for (HoneypotBlockObject pot : pots) {
					if (pot.getBlock().getType().equals(Material.AIR)) {
						Honeypot.getPlugin().getLogger().info("Found ghost Honeypot at " + pot.getCoordinates() + ". Removing");
						Honeypot.getHoneypotLogger().log("Found ghost Honeypot at " + pot.getCoordinates() + ". Removing");
						Honeypot.getHBM().deleteBlock(pot.getBlock());
						removedPots++;
					}
				}
				
				Honeypot.getPlugin().getLogger().info("Finished ghost Honeypot checks! Removed " + removedPots + " ghost Honeypots.");
				Honeypot.getHoneypotLogger().log("Finished ghost Honeypot checks! Removed " + removedPots + " ghost Honeypots.");
			}

		}, 0L, 20L * 60 * HoneypotConfigManager.getPluginConfig().getInt("ghost-honeypot-checker.check-interval"));
	}

	/**
	 * Cancel the ghost honeypot task
	 */
	public static void cancelTask() {
		task.cancel();
	}

}
