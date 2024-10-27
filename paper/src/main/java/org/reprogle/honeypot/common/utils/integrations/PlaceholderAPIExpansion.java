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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

@SuppressWarnings({ "deprecation", "unused" })
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final Honeypot plugin;
    private final HoneypotPlayerManager playerManager;
    private final HoneypotConfigManager configManager;
    private final HoneypotLogger logger;

    @Inject
    public PlaceholderAPIExpansion(Honeypot plugin, HoneypotLogger logger, HoneypotPlayerManager playerManager, HoneypotConfigManager configManager) {
        this.plugin = plugin;
        this.logger = logger;
        this.playerManager = playerManager;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "honeypot";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull
    String params) {
        logger.debug(Component.text("Param received was: " + params));
        if (params.equalsIgnoreCase("current_count_broken")) {
            if (player == null)
                return null;
            int count = playerManager.getCount(player);
            return count < 0 ? "0" : String.valueOf(count);
        }

        if (params.startsWith("current_count_broken_")) {
            String playerName = params.split("current_count_broken_")[1];
            OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
            int count = playerManager.getCount(p);
            return count < 0 ? "0" : String.valueOf(count);
        }

        if (params.equalsIgnoreCase("breaks_before_action")) {
            return String.valueOf(configManager.getPluginConfig().getInt("blocks-broken-before-action-taken"));
        }

        return null;
    }

}
