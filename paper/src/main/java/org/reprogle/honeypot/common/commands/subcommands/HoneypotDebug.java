/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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

package org.reprogle.honeypot.common.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

@SuppressWarnings("java:S1192")
public class HoneypotDebug implements HoneypotSubCommand {

    private final Honeypot plugin;
    private final CommandFeedback commandFeedback;
    private final HoneypotConfigManager configManager;

    @Inject
    public HoneypotDebug(Honeypot plugin, CommandFeedback commandFeedback, HoneypotConfigManager configManager) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        NamespacedKey key = new NamespacedKey(plugin, "honeypot-debug-enabled");

        if (!configManager.getPluginConfig().getString("storage-method").equalsIgnoreCase("pdc")) {
            p.sendMessage(commandFeedback.sendCommandFeedback("debug"));
            return;
        }

        if (p.getPersistentDataContainer().has(key)) {
            p.getPersistentDataContainer().remove(key);
            p.sendMessage(commandFeedback.sendCommandFeedback("debug", false));
        } else {
            p.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            p.sendMessage(commandFeedback.sendCommandFeedback("debug", true));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.debug"));
        return permissions;
    }

}
