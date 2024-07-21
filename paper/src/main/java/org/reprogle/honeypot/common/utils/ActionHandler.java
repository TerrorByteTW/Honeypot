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

package org.reprogle.honeypot.common.utils;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import me.clip.placeholderapi.PlaceholderAPI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;

import java.util.List;

public class ActionHandler {

    private final Honeypot plugin;
    private final HoneypotLogger logger;
    private final HoneypotConfigManager configManager;
    private final CommandFeedback commandFeedback;

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Inject
    public ActionHandler(Honeypot plugin, HoneypotLogger logger, HoneypotConfigManager configManager, CommandFeedback commandFeedback) {
        this.plugin = plugin;
        this.logger = logger;
        this.configManager = configManager;
        this.commandFeedback = commandFeedback;
    }

    @SuppressWarnings({"java:S3776", "java:S2629", "java:S1192", "java:S6541"})
    public void handleCustomAction(String action, Block block, Player player) {

        plugin.getHoneypotLogger().debug(Component.text("Handling action " + action + " for player " + player.getName() + " at location " + block.getLocation()));

        // Behavior providers take higher precedence over custom config actions.
        if (plugin.getRegistry().getBehaviorProvider(action) != null) {
            Honeypot.processor.process(plugin.getRegistry().getBehaviorProvider(action), player, block);
            return;
        }

        // Default path is likely due to custom actions. Run whatever the action was
        YamlDocument config = configManager.getHoneypotsConfig();
        if (config.contains(action)) {
            List<String> commands = config.getStringList(action + ".commands");
            List<String> permissionsAdd = config.getStringList(action + ".permissions-add");
            List<String> permissionsRemove = config.getStringList(action + ".permissions-remove");
            List<String> broadcasts = config.getStringList(action + ".broadcasts");
            List<String> messages = config.getStringList(action + ".messages");

            if (!commands.isEmpty()) {
                for (String command : commands) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                            formatMessage(command, block, player, true).toString());
                }
            }

            if (!messages.isEmpty()) {
                for (String message : messages) {
                    player.sendMessage(formatMessage(message, block, player, false));
                }
            }

            if (!broadcasts.isEmpty()) {
                for (String broadcast : broadcasts) {
                    plugin.getServer().broadcast(formatMessage(broadcast, block, player, false));
                }
            }

            if (plugin.getAdapterManager().getPermissions() != null) {
                if (!permissionsAdd.isEmpty()) {
                    for (String permission : permissionsAdd) {
                        plugin.getAdapterManager().getPermissions().getPermissionProvider().playerAdd(null, player, permission);
                    }
                }

                if (!permissionsRemove.isEmpty()) {
                    for (String permission : permissionsRemove) {
                        plugin.getAdapterManager().getPermissions().getPermissionProvider().playerRemove(null, player, permission);
                    }
                }
            }
            // I'd like to warn them if the tried to adjust permissions without vault. If vault is null and they
            // *didn't* try to adjust permissions, then who cares?
            else if (!permissionsAdd.isEmpty() || !permissionsRemove.isEmpty()) {
                logger.warning(commandFeedback.getChatPrefix().append(Component.text(" Vault is not installed, Honeypots that modify permissions won't work. Please download here: https://www.spigotmc.org/resources/vault.34315/", NamedTextColor.RED)));
            }
        } else {
            logger.warning(Component.text("A Honeypot tried to run using action: " + action + ", but that action doesn't exist! Please verify your honeypots.yml config"));
        }
    }

    private Component formatMessage(String message, Block block, Player player, boolean command) {
        String formattedString = message.replace("%player%", player.getName())
                .replace("%pLocation%", player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ())
                .replace("%bLocation%", block.getLocation().getX() + " " + block.getLocation().getY() + " " + block.getLocation().getZ())
                .replace("%world%", block.getLocation().getWorld().getName());

        // Support Placeholder API!!!! This will parse any remaining placeholders in the message
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            formattedString = PlaceholderAPI.setPlaceholders(player, formattedString);

        return command ? Component.text(formattedString) : Component.text(mm.deserialize(formattedString).toString());
    }
}
