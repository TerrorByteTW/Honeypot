/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.store.HoneypotBlockManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;

import java.util.List;

@SuppressWarnings({"java:S1604"})
@Singleton
public class GhostHoneypotMonitor {
    private final BytePluginConfig config;
    private final HoneypotLogger logger;
    private final HoneypotBlockManager blockManager;
    private final JavaPlugin plugin;
    private ScheduledTask task;

    // Create package constructor to hide implicit one
    @Inject
    public GhostHoneypotMonitor(JavaPlugin plugin, HoneypotLogger logger, HoneypotBlockManager blockManager, BytePluginConfig config) {
        this.plugin = plugin;
        this.logger = logger;
        this.blockManager = blockManager;
        this.config = config;

        // Start the GhostHoneypotMonitor
        if (config.config().getBoolean("ghost-honeypot-checker.enable")) {
            logger.debug(
                    Component.text("Ghost Honeypot Checker is enabled, starting the monitoring task. If you need to change the settings for this function, edit the config then do /honeypot reload"), false);
        }
    }

    /**
     * Start a task to check for ghost honeypots every defined interval
     */
    public void startTask() {
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            logger.debug(Component.text("Checking for Ghost Honeypots..."), true);
            int removedPots = 0;
            List<World> worlds = Bukkit.getWorlds();

            // Normally, with SQLite we could just query the entire table and loop through blocks. However, with
            // legacy storage providers (And some custom ones), blocks *must* be allocated to a specific world.
            // Eventually this will be better.
            // TODO: Make this loop better (10/26/24 No better permanent fix like a temporary solution.
            //  Git blame says I made this change on 2/20/2024, almost a year ago. I'll get around to it eventually) ^^^
            //  3/28/2026 - Lol still haven't
            for (World world : worlds) {
                List<HoneypotBlockObject> pots = blockManager.getAllHoneypots(world);
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
                        logger.warning(Component.text("Could not get the material for Honeypot at " + pot.getCoordinates() + " because the world isn't loaded yet (Maybe running Folia?)"));
                        continue;
                    }

                    /*
                     * Check for water and lava in the event that BlockFromToEvent has been disabled
                     * in config, meaning that water and lava may end up occupying Honeypot spaces
                     * in some instances (Such as if a Honeypot was set as a torch)
                     */
                    if (block.equals(Material.AIR) || block.equals(Material.WATER) || block.equals(Material.LAVA)) {
                        logger.debug(Component.text("Found ghost Honeypot at " + pot.getCoordinates() + " in world " + pot.getWorld() + ". Removing"), true);
                        blockManager.deleteBlock(pot.getBlock());
                        removedPots++;
                    }
                }
            }

            logger.debug(Component.text("Finished ghost Honeypot checks! Removed " + removedPots + " ghost Honeypots."), true);
        }, 10L, 20L * 60 * config.config().getInt("ghost-honeypot-checker.check-interval"));
    }

    /**
     * Cancel the ghost honeypot task
     */
    public void cancelTask() {
        task.cancel();
    }

}
