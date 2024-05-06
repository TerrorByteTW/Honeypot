/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2023
 * Copyright Honeypot Contributors (c) 2023
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

package org.reprogle.honeypot.common.utils.folia;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import static org.reprogle.honeypot.common.utils.folia.Scheduler.FOLIA;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public abstract class ScheduledRunnable implements Runnable {

	private Object task;

	public synchronized boolean isCancelled() {
		checkScheduled();
		if (FOLIA) {
			return ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).isCancelled();
		} else {
			return ((BukkitTask) task).isCancelled();
		}
	}

	public synchronized void cancel() {
		checkScheduled();
		if (FOLIA) {
			((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
		} else {
			((BukkitTask) task).cancel();
		}
	}

	public synchronized Scheduler.ScheduledTask runTask(Plugin plugin, Entity entity) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(entity.getScheduler().run(plugin, scheduledTask -> run(), null));
		} else {
			return setupTask(Bukkit.getScheduler().runTask(plugin, this));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskLater(Plugin plugin, long delay, Entity entity) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(entity.getScheduler().runDelayed(plugin, scheduledTask -> run(), null, delay));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskLater(plugin, this, delay));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskTimer(Plugin plugin, long delay, long period, Entity entity) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> run(), null, delay, period));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period));
		}
	}

	public synchronized Scheduler.ScheduledTask runTask(Plugin plugin, Location location) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> run()));
		} else {
			return setupTask(Bukkit.getScheduler().runTask(plugin, this));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskLater(Plugin plugin, long delay, Location location) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> run(), delay));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskLater(plugin, this, delay));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskTimer(Plugin plugin, long delay, long period,
			Location location) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> run(), delay,
					period));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period));
		}
	}

	public synchronized Scheduler.ScheduledTask runTask(Plugin plugin) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> run()));
		} else {
			return setupTask(Bukkit.getScheduler().runTask(plugin, this));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskLater(Plugin plugin, long delay) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> run(), delay));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskLater(plugin, this, delay));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskTimer(Plugin plugin, long delay, long period) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(
					Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> run(), delay, period));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskAsynchronously(Plugin plugin) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> run()));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, this));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskLaterAsynchronously(Plugin plugin, long delay) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> run(), delay * 50,
					TimeUnit.MILLISECONDS));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, delay));
		}
	}

	public synchronized Scheduler.ScheduledTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) {
		checkNotYetScheduled();
		if (FOLIA) {
			return setupTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> run(),
					Math.max(1, delay * 50), period * 50, TimeUnit.MILLISECONDS));
		} else {
			return setupTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period));
		}
	}

	private void checkScheduled() {
		if (task == null) {
			throw new IllegalStateException("Not scheduled yet");
		}
	}

	private void checkNotYetScheduled() {
		if (task != null) {
			throw new IllegalStateException("Already scheduled as " + task);
		}
	}

	private Scheduler.ScheduledTask setupTask(Object task) {
		this.task = task;
		return new Scheduler.ScheduledTask(task);
	}
}
