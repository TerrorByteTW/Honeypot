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

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"java:S1192", "java:S3776", "deprecation"})
public class HoneypotHistory implements HoneypotSubCommand {

    private final CommandFeedback commandFeedback;
    private final HoneypotConfigManager configManager;
    private final HoneypotPlayerHistoryManager playerHistoryManager;

    @Inject
    public HoneypotHistory(CommandFeedback commandFeedback, HoneypotConfigManager configManager, HoneypotPlayerHistoryManager playerHistoryManager) {
        this.commandFeedback = commandFeedback;
        this.configManager = configManager;
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

            if (argPlayer == null || !Bukkit.getPlayer(args[2]).isOnline()) {
                p.sendMessage(commandFeedback.sendCommandFeedback("not-online"));
                return;
            }

            if (args[1].equalsIgnoreCase("query")) {
                p.sendMessage(commandFeedback.sendCommandFeedback("searching"));

                List<HoneypotPlayerHistoryObject> history = playerHistoryManager.getPlayerHistory(argPlayer);
                int length = configManager.getPluginConfig().getInt("history-length");

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


                    Component playerInfo = Component.text("Player: ").color(NamedTextColor.GOLD)
                            .append(Component.text(history.get(i).getPlayer(), NamedTextColor.WHITE))
                            .append(Component.text(" @ ", NamedTextColor.WHITE))
                            .append(Component.text(history.get(i).getHoneypot().getWorld() + " ", NamedTextColor.GOLD))
                            .append(Component.text(history.get(i).getHoneypot().getCoordinates(), NamedTextColor.WHITE));

                    playerInfo.clickEvent(ClickEvent.runCommand("/hpteleport "
                            + (history.get(i).getHoneypot().getLocation().getX() + 0.5) + " "
                            + (history.get(i).getHoneypot().getLocation().getY() + 1) + " "
                            + (history.get(i).getHoneypot().getLocation().getZ() + 0.5)));

                    playerInfo.hoverEvent(HoverEvent.showText(Component.text("Click to teleport").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)));

                    p.sendMessage(playerInfo);
                    p.sendMessage(Component.text("Action: ").append(Component.text(history.get(i).getHoneypot().getAction(), NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("Break type: ").append(Component.text(history.get(i).getType(), NamedTextColor.GOLD)));
                    p.sendMessage(Component.text("----------------------------------", NamedTextColor.GOLD));
                }

            } else if (args[1].equalsIgnoreCase("delete")) {
                if (args.length >= 4) {
                    playerHistoryManager.deletePlayerHistory(argPlayer,
                            Integer.parseInt(args[3]));
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
