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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"java:S1192", "java:S3776"})
public class HoneypotHistory implements HoneypotSubCommand {

    private final CommandFeedback commandFeedback;
    private final BytePluginConfig config;
    private final HoneypotPlayerHistoryManager playerHistoryManager;

    @Inject
    public HoneypotHistory(CommandFeedback commandFeedback, BytePluginConfig config, HoneypotPlayerHistoryManager playerHistoryManager) {
        this.commandFeedback = commandFeedback;
        this.config = config;
        this.playerHistoryManager = playerHistoryManager;

    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public void perform(Player p, String[] args) {
        if (args.length >= 3 && (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("query"))) {
            Player argPlayer = Bukkit.getPlayer(args[2]);

            if (argPlayer == null || !argPlayer.isOnline()) {
                p.sendMessage(commandFeedback.sendCommandFeedback("not-online"));
                return;
            }

            if (args[1].equalsIgnoreCase("query")) {
                p.sendMessage(commandFeedback.sendCommandFeedback("searching"));

                List<HoneypotPlayerHistoryObject> history = playerHistoryManager.getPlayerHistory(argPlayer);
                int length = config.config().getInt("history-length");

                if (history.size() > length) {
                    p.sendMessage(commandFeedback.sendCommandFeedback("truncating"));
                    history = history.subList(0, length);
                }

                if (history.isEmpty()) {
                    p.sendMessage(commandFeedback.sendCommandFeedback("no-history"));
                    return;
                }

                // Reverse the history array so that it's in chronological order when sent to the player
                Collections.reverse(history);

                int limit = Math.min(history.size(), length);

                for (int i = 0; i < limit; i++) {
                    p.sendMessage(
                            Component.text("\n-------[ ", NamedTextColor.GOLD)
                                    .append(Component.text(history.get(i).getDateTime(), NamedTextColor.WHITE))
                                    .append(Component.text(" ]-------", NamedTextColor.GOLD))
                    );

                    Location location = history.get(i).getHoneypot().getLocation();

                    Component playerInfo = Component.text("Player: ").color(NamedTextColor.GOLD)
                            .append(Component.text(history.get(i).getPlayer(), NamedTextColor.WHITE))
                            .append(Component.text(" @ ", NamedTextColor.WHITE))
                            .append(Component.text(history.get(i).getHoneypot().getWorld() + " ", NamedTextColor.GOLD))
                            .append(Component.text(history.get(i).getHoneypot().getCoordinates(), NamedTextColor.WHITE))
                            .clickEvent(ClickEvent.callback((Audience audience) -> {
                                Player player = (Player) audience;
                                player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to teleport").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)));

                    p.sendMessage(playerInfo);
                    p.sendMessage(Component.text("Action: ").append(Component.text(history.get(i).getHoneypot().getAction(), NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("Break type: ").append(Component.text(history.get(i).getType(), NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("----------------------------------", NamedTextColor.GOLD));
                }

            } else if (args[1].equalsIgnoreCase("delete")) {
                if (args.length >= 4) {
                    int count = 5;
                    try {
                        // Clamp to 100,000. No need to go insane here, and 100,000 is already pushing it tbh haha.
                        count = Math.max(0, Math.min(Integer.parseInt(args[3]), 100000));
                    } catch (NumberFormatException ignored) {
                        // Ignored, since the default for `count` is already 5, so we don't need to reassign it again.
                    }

                    playerHistoryManager.deletePlayerHistory(argPlayer, count);
                } else {
                    playerHistoryManager.deletePlayerHistory(argPlayer);
                }
                p.sendMessage(commandFeedback.sendCommandFeedback("success"));
            }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("purge")) {
            playerHistoryManager.deleteAllHistory();
            p.sendMessage(commandFeedback.sendCommandFeedback("success"));
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("usage"));
        }

    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        // Base arguments
        if (args.length == 2) {
            subcommands.add("delete");
            subcommands.add("query");
            subcommands.add("purge");
            // If the args length is 3, and they passed a valid sub-subcommand, do this
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("query") || args[1].equalsIgnoreCase("delete"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                subcommands.add(player.getName());
            }
            // If the args length is 4, and they typed delete, just give them a list of numbers
        } else if (args.length == 4 && args[1].equalsIgnoreCase("delete")) {
            for (int i = 1; i < 10; i++) {
                subcommands.add(Integer.toString(i));
            }
        }

        return subcommands;
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.history"));
        return permissions;
    }

}
