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

package org.reprogle.honeypot.common.commands.subcommands;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HoneypotLocate implements HoneypotSubCommand {

    private final Honeypot plugin;
    private final HoneypotConfigManager configManager;
    private final CommandFeedback commandFeedback;

    @Inject
    public HoneypotLocate(Honeypot plugin, HoneypotConfigManager configManager, CommandFeedback commandFeedback) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.commandFeedback = commandFeedback;
    }

    @Override
    public String getName() {
        return "locate";
    }

    /*
     * TODO - Add some sort of functionality to display all Honeypots within render distance. Not sure how to do that but I'll figure it out.
     *  Maybe consider integrating maps plugins? However, that would ruin the whole point of honeypot, to be secretive. I'll figure it out.
     */
    @Override
    public void perform(Player p, String[] args) {
        int radius;
        if (args.length == 2) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(commandFeedback.sendCommandFeedback("invalid-radius"));
                radius = configManager.getPluginConfig().getInt("search-range");
            }
        } else {
            radius = configManager.getPluginConfig().getInt("search-range");
        }

        boolean potFound = false;

        List<HoneypotBlockObject> honeypots = Registry.getStorageProvider().getNearbyHoneypots(p.getLocation(), radius);
        if (!honeypots.isEmpty()) potFound = true;

        for (HoneypotBlockObject honeypot : honeypots) {
            Slime slime = (Slime) Objects.requireNonNull(Bukkit.getWorld(honeypot.getBlock().getWorld().getName()))
                    .spawnEntity(honeypot.getBlock().getLocation().add(0.5, 0, 0.5), EntityType.SLIME);
            slime.setSize(2);
            slime.setAI(false);
            slime.setGlowing(true);
            slime.setInvulnerable(true);
            slime.setHealth(4.0);
            slime.setInvisible(true);

            // Remove the slime after 5 seconds
            // If we kill it, a death animation plays and the slime splits and drops items
            slime.getScheduler().runDelayed(plugin, scheduledTask -> slime.remove(), null, 20L * 5);
        }

        // Let the player know if a pot was found or not
        if (potFound) {
            p.sendMessage(commandFeedback.sendCommandFeedback("found-pots"));
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
        }
    }

    // We don't have any subcommands here, but we cannot return null otherwise the
    // tab completer in the CommandManager
    // will throw an exception since CopyPartialMatches doesn't allow null values
    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.locate"));
        return permissions;
    }
}
