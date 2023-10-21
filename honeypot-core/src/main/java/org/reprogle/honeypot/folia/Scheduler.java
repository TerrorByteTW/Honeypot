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

package org.reprogle.honeypot.folia;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

@SuppressWarnings({ "deprecation" })
public class Scheduler {

	public static final boolean FOLIA;

	static {
		boolean folia;
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
			folia = true;
		} catch (ClassNotFoundException e) {
			folia = false;
		}

		FOLIA = folia;
	}

	public static void executeOrScheduleSync(Plugin plugin, Runnable task, Entity entity) {
		if (FOLIA) {
			if (Bukkit.isOwnedByCurrentRegion(entity)) {
				task.run();
			} else {
				entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
			}
		} else {
			if (Bukkit.isPrimaryThread()) {
				task.run();
			} else {
				Bukkit.getScheduler().runTask(plugin, task);
			}
		}
	}

	public static void executeOrScheduleSync(Plugin plugin, Runnable task, Location location) {
		if (FOLIA) {
			if (Bukkit.isOwnedByCurrentRegion(location)) {
				task.run();
			} else {
				Bukkit.getRegionScheduler().execute(plugin, location, task);
			}
		} else {
			if (Bukkit.isPrimaryThread()) {
				task.run();
			} else {
				Bukkit.getScheduler().runTask(plugin, task);
			}
		}
	}

	public static ScheduledTask runTask(Plugin plugin, Runnable task, Entity entity) {
		if (FOLIA) {
			return new ScheduledTask(entity.getScheduler().run(plugin, scheduledTask -> task.run(), null));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTask(plugin, task));
		}
	}

	public static ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay, Entity entity) {
		if (FOLIA) {
			return new ScheduledTask(
					entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
		}
	}

	public static ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period, Entity entity) {
		if (FOLIA) {
			return new ScheduledTask(entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), null,
					Math.max(1, delay), period));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
		}
	}

	public static ScheduledTask runTask(Plugin plugin, Runnable task, Location location) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run()));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTask(plugin, task));
		}
	}

	public static ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay, Location location) {
		if (FOLIA) {
			return new ScheduledTask(
					Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
		}
	}

	public static ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period, Location location) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location,
					scheduledTask -> task.run(), Math.max(1, delay), period));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
		}
	}

	public static ScheduledTask runTask(Plugin plugin, Runnable task) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run()));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTask(plugin, task));
		}
	}

	public static ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay) {
		if (FOLIA) {
			return new ScheduledTask(
					Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
		}
	}

	public static ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
					scheduledTask -> task.run(), Math.max(1, delay), period));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
		}
	}

	public static ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable task) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run()));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
		}
	}

	public static ScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(),
					delay * 50, TimeUnit.MILLISECONDS));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay));
		}
	}

	public static ScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
		if (FOLIA) {
			return new ScheduledTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(),
					Math.max(1, delay * 50), period * 50, TimeUnit.MILLISECONDS));
		} else {
			return new ScheduledTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period));
		}
	}

	public static class ScheduledTask {

		private final Object task;

		public ScheduledTask(Object task) {
			this.task = task;
		}

		public boolean isCancelled() {
			if (FOLIA) {
				return ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).isCancelled();
			} else {
				return ((BukkitTask) task).isCancelled();
			}
		}

		public void cancel() {
			if (FOLIA) {
				((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
			} else {
				((BukkitTask) task).cancel();
			}
		}

		public Plugin getOwner() {
			if (FOLIA) {
				return ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).getOwningPlugin();
			} else {
				return ((BukkitTask) task).getOwner();
			}
		}

	}

}
