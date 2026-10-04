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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.store.HoneypotPlayerManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final HoneypotPlayerManager playerManager;
    private final BytePluginConfig config;
    private final HoneypotLogger logger;

    private static final String CURRENT_COUNT = "current_count_";
    private static final String TRIGGERS_BEFORE_ACTION = "triggers_before_action_";

    // Longest names first, so a name that's a prefix of another can't match it by mistake. "broken" is kept for backwards compatibility
    private static final List<Map.Entry<String, TriggerType>> TRIGGER_TYPE_NAMES = List.of(
        Map.entry("inventory_interact", TriggerType.INVENTORY_INTERACT),
        Map.entry("inventory_open", TriggerType.INVENTORY_OPEN),
        Map.entry("generic", TriggerType.GENERIC),
        Map.entry("broken", TriggerType.BREAK),
        Map.entry("break", TriggerType.BREAK)
    );

    @Inject
    public PlaceholderAPIExpansion(JavaPlugin plugin, HoneypotLogger logger, HoneypotPlayerManager playerManager, BytePluginConfig config) {
        this.plugin = plugin;
        this.logger = logger;
        this.playerManager = playerManager;
        this.config = config;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "honeypot";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Supported placeholders. {@code <type>} is one of {@code break} (or {@code broken}), {@code inventory_open},
     * {@code inventory_interact} or {@code generic}. Any placeholder ending in {@code [_<player>]} returns the viewing
     * player's value, or the named player's value if a name is appended.
     * <ul>
     *     <li>{@code %honeypot_current_count_<type>[_<player>]%} - The player's current count for a trigger type, which resets when an action is taken</li>
     *     <li>{@code %honeypot_lifetime_triggers[_<player>]%} - The total number of Honeypots the player has ever triggered</li>
     *     <li>{@code %honeypot_lifetime_actions[_<player>]%} - The total number of actions ever taken against the player</li>
     *     <li>{@code %honeypot_triggers_before_action_<type>%} - The configured trigger limit for a trigger type</li>
     *     <li>{@code %honeypot_breaks_before_action%} - The configured trigger limit for block breaks</li>
     * </ul>
     */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        logger.debug(Component.text("Param received was: " + params));

        if (params.equalsIgnoreCase("breaks_before_action"))
            return triggerLimit(TriggerType.BREAK);

        if (startsWithIgnoreCase(params, TRIGGERS_BEFORE_ACTION)) {
            String typeName = params.substring(TRIGGERS_BEFORE_ACTION.length());
            for (Map.Entry<String, TriggerType> type : TRIGGER_TYPE_NAMES) {
                if (typeName.equalsIgnoreCase(type.getKey()))
                    return triggerLimit(type.getValue());
            }
            return null;
        }

        if (startsWithIgnoreCase(params, CURRENT_COUNT)) {
            String rest = params.substring(CURRENT_COUNT.length());
            for (Map.Entry<String, TriggerType> type : TRIGGER_TYPE_NAMES) {
                String playerName = matchPlaceholder(rest, type.getKey());
                if (playerName != null)
                    return playerStat(player, playerName, p -> playerManager.getCount(p, type.getValue()));
            }
            return null;
        }

        String playerName = matchPlaceholder(params, "lifetime_triggers");
        if (playerName != null)
            return playerStat(player, playerName, playerManager::getLifetimeTriggers);

        playerName = matchPlaceholder(params, "lifetime_actions");
        if (playerName != null)
            return playerStat(player, playerName, playerManager::getLifetimeActions);

        return null;
    }

    private String triggerLimit(TriggerType type) {
        return String.valueOf(config.config().getInt(ActionHandler.triggerLimitPath(type), 1));
    }

    /**
     * Matches {@code params} against {@code name} or {@code name_<player>}
     *
     * @return An empty string if {@code params} is exactly {@code name}, the player name if a player was appended, or
     * null if {@code params} doesn't match
     */
    private static String matchPlaceholder(String params, String name) {
        if (params.equalsIgnoreCase(name))
            return "";

        if (params.length() > name.length() + 1 && startsWithIgnoreCase(params, name + "_"))
            return params.substring(name.length() + 1);

        return null;
    }

    /**
     * Gets a stat for the viewing player, or for the named player if {@code playerName} isn't empty
     */
    private static String playerStat(OfflinePlayer viewer, String playerName, ToIntFunction<OfflinePlayer> stat) {
        // getOfflinePlayerIfCached avoids a blocking Mojang lookup for players who have never joined
        OfflinePlayer target = playerName.isEmpty() ? viewer : Bukkit.getOfflinePlayerIfCached(playerName);
        if (target == null)
            return playerName.isEmpty() ? null : "0";

        return String.valueOf(Math.max(0, stat.applyAsInt(target)));
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

}
