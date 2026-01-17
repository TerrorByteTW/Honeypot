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
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.CacheManager;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;
import org.reprogle.honeypot.common.utils.GhostHoneypotFixer;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HoneypotReload implements HoneypotSubCommand {

    private final HoneypotConfigManager configManager;
    private final GhostHoneypotFixer fixer;
    private final CommandFeedback commandFeedback;
    private final Honeypot plugin;
    private final HoneypotLogger logger;

    @Inject
    public HoneypotReload(Honeypot plugin, HoneypotConfigManager configManager, GhostHoneypotFixer fixer, CommandFeedback commandFeedback, HoneypotLogger logger) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.fixer = fixer;
        this.commandFeedback = commandFeedback;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void perform(Player p, String[] args) {
        try {
            configManager.getPluginConfig().reload();
            configManager.getPluginConfig().save();

            configManager.getGuiConfig().reload();
            configManager.getGuiConfig().save();

            configManager.getHoneypotsConfig().reload();
            configManager.getHoneypotsConfig().save();

            configManager.getLanguageFile().reload();
            configManager.getLanguageFile().save();

            fixer.cancelTask();
            if (configManager.getPluginConfig().getBoolean("ghost-honeypot-checker.enable")) {
                fixer.startTask();
            }

            CacheManager.clearCache();

            String providerName = configManager.getPluginConfig().getString("storage-method");
            if (!Registry.getStorageProvider().getProviderName().equalsIgnoreCase(providerName)) {
                StorageProvider provider = Registry.getStorageManagerRegistry().getStorageProvider(providerName);
                if (provider != null) {
                    if (!configManager.getPluginConfig().getBoolean("allow-third-party-storage-providers")) {
                        logger.severe(Component.text("The storage method was updated to a custom provider, but the server is not configured to allow third-party storage providers! On your next reboot Honeypot WILL crash ON PURPOSE! Please validate your config"));
                    }
                    Registry.setStorageProvider(provider);
                    logger.info(Component.text("The storage provider was updated to \"" + providerName + "\""));
                } else {
                    logger.severe(Component.text("The storage provider was updated to \"" + providerName + "\" but it is not registered! On your next reboot Honeypot WILL crash ON PURPOSE! Please validate your config"));
                }
            }

            p.sendMessage(commandFeedback.sendCommandFeedback("reload"));
            logger.info(Component.text("Honeypot has successfully been reloaded"));
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not load config file, disabling! Please alert the plugin author with the following info:"
                            + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
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
        permissions.add(new HoneypotPermission("honeypot.reload"));
        return permissions;
    }
}
