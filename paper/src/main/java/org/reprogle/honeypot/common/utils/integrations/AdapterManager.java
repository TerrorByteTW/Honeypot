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

package org.reprogle.honeypot.common.utils.integrations;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

@Singleton
public class AdapterManager {

    private final JavaPlugin plugin;
    private final HoneypotLogger logger;
    private final BytePluginConfig config;
    private final CommandFeedback commandFeedback;
    private WorldGuardAdapter wga = null;
    private GriefPreventionAdapter gpa = null;
    private LandsAdapter la = null;
    private PermissionAdapter pa = null;
    private final Injector injector;

    /**
     * Private constructor to hide implicit one
     *
     * @param plugin          Plugin instance
     * @param config          ConfigManager instance
     * @param commandFeedback CommandFeedback instance
     */
    @Inject
    public AdapterManager(JavaPlugin plugin, HoneypotLogger logger, BytePluginConfig config, CommandFeedback commandFeedback, Injector injector) {
        this.config = config;
        this.logger = logger;
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.injector = injector;
    }

    public void onLoadAdapters(Server server) {
        if (server.getPluginManager().getPlugin("WorldGuard") != null) {
            wga = new WorldGuardAdapter();
        }
    }

    public void onEnableAdapters(Server server) {
        if (server.getPluginManager().getPlugin("GriefPrevention") != null)
            gpa = new GriefPreventionAdapter(config);

        if (server.getPluginManager().getPlugin("Lands") != null) {
            la = new LandsAdapter(plugin);
        }

        if (server.getPluginManager().getPlugin("Vault") != null) {
            pa = new PermissionAdapter(plugin);
        } else {
            logger.info(commandFeedback.getChatPrefix()
                .append(Component.text("Vault is not installed, some features won't work. Please download Vault here: https://www.spigotmc.org/resources/vault.34315/", NamedTextColor.RED)));
        }

        if (server.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.debug(Component.text("PlaceholderAPI is installed on this server, hooking into it"), false);
            injector.getInstance(PlaceholderAPIExpansion.class).register();
        }
    }

    /**
     * Retrieve the WorldGuard Adapter
     *
     * @return WorldGuardAdapter
     */
    public WorldGuardAdapter getWorldGuardAdapter() {
        return wga;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     *
     * @return GriefPreventionAdapter
     */
    public GriefPreventionAdapter getGriefPreventionAdapter() {
        return gpa;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     *
     * @return LandsAdapter
     */
    public LandsAdapter getLandsAdapter() {
        return la;
    }

    /**
     * Retrieve the permission service provider object
     *
     * @return {@link Permission}
     */
    public PermissionAdapter getPermissions() {
        return pa;
    }

    /**
     * Checks if any adapter blocks the action. Uses two locations to support regions
     * This method uses a single, reusable Location object to avoid creating new objects for each iteration.
     *
     * @return True if all adapters say the action is allowed
     */
    public boolean checkAllAdapters(Player player, Location pos1, Location pos2) {
        World world = pos1.getWorld();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Location reusable = new Location(world, 0, 0, 0);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    reusable.set(x, y, z);

                    if (la != null && !la.isAllowed(reusable)) {
                        return false;
                    }

                    if (gpa != null && !gpa.isAllowed(player, reusable)) {
                        return false;
                    }

                    if (wga != null && !wga.isAllowed(player, reusable)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks if any adapter blocks the action
     * @param player The player to check against
     * @param location The location to check
     * @return True if all adapters say the action is allowed, false if not
     */
    public boolean checkAllAdapters(Player player, Location location) {
        return (la == null || la.isAllowed(location))
            && (gpa == null || gpa.isAllowed(player, location))
            && (wga == null || wga.isAllowed(player, location));
    }
}
