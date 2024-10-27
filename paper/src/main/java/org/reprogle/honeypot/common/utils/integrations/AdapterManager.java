/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright TerrorByte & Honeypot Contributors (c) 2022 - 2024
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

package org.reprogle.honeypot.common.utils.integrations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

@Singleton
public class AdapterManager {

    private final Honeypot plugin;
    private final HoneypotConfigManager configManager;
    private final CommandFeedback commandFeedback;
    private WorldGuardAdapter wga = null;
    private GriefPreventionAdapter gpa = null;
    private LandsAdapter la = null;
    private PermissionAdapter pa = null;

    /**
     * Private constructor to hide implicit one
     *
     * @param plugin          Pluign instance
     * @param configManager   ConfigManager instance
     * @param commandFeedback CommandFeedback instance
     */
    @Inject
    public AdapterManager(Honeypot plugin, HoneypotConfigManager configManager, CommandFeedback commandFeedback) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
    }

    public void onLoadAdapters(Server server) {
        if (server.getPluginManager().getPlugin("WorldGuard") != null) {
            wga = new WorldGuardAdapter();
        }
    }

    public void onEnableAdapters(Server server) {
        if (server.getPluginManager().getPlugin("GriefPrevention") != null)
            gpa = new GriefPreventionAdapter(configManager);

        if (server.getPluginManager().getPlugin("Lands") != null) {
            la = new LandsAdapter(plugin);
        }

        if (server.getPluginManager().getPlugin("Vault") != null) {
            pa = new PermissionAdapter(plugin);
        } else {
            plugin.getHoneypotLogger().info(commandFeedback.getChatPrefix()
                    .append(Component.text("Vault is not installed, some features won't work. Please download Vault here: https://www.spigotmc.org/resources/vault.34315/", NamedTextColor.RED)));
        }

        if (server.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            plugin.getHoneypotLogger().debug(Component.text("PlaceholderAPI is installed on this server, hooking into it"));
            plugin.getInjector().getInstance(PlaceholderAPIExpansion.class).register();
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
     * Checks if any adapter blocks the action
     *
     * @return True if all adapters say the action is allowed
     */
    public boolean checkAllAdapters(Player player, Location location) {
        return (la == null || la.isAllowed(location)) &&
                (gpa == null || gpa.isAllowed(player, location)) &&
                (wga == null || wga.isAllowed(player, location));
    }
}
