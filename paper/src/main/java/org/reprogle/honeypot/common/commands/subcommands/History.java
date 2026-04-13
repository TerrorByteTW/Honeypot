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
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotPlayerHistoryObject;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"java:S1192", "java:S3776"})
public class History implements CommandCallback {

    private final CommandFeedback commandFeedback;
    private final BytePluginConfig config;
    private final HoneypotPlayerHistoryManager playerHistoryManager;

    @Inject
    public History(CommandFeedback commandFeedback, BytePluginConfig config, HoneypotPlayerHistoryManager playerHistoryManager) {
        this.commandFeedback = commandFeedback;
        this.config = config;
        this.playerHistoryManager = playerHistoryManager;

    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        HoneypotHistoryArgs args = parseArgumentsFromContext(ctx);
        if (!args.isValid()) {
            ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("usage"));
            return Command.SINGLE_SUCCESS;
        }

        // This is safe because args.isValid() will only be true if both of these variables are not null
        assert args.action != null;
        assert args.player != null;

        var sender = ctx.getSource().getSender();

        switch (args.action) {
            case "delete":
                if (!args.player.isOnline()) {
                    sender.sendMessage(commandFeedback.sendCommandFeedback("not-online"));
                    return Command.SINGLE_SUCCESS;
                }

                if (args.count >= 1) { // Since primitives are not nullable, the argument has a minimum of 1 but defaults to 0 if not provided. So, we know that args.count == 0 means not provided, and anything <= 0 is not possible thanks to Brigadier
                    playerHistoryManager.deletePlayerHistory(args.player, args.count);
                } else {
                    playerHistoryManager.deletePlayerHistory(args.player);
                }

                sender.sendMessage(commandFeedback.sendCommandFeedback("success"));

                break;
            case "query":
                if (!args.player.isOnline()) {
                    sender.sendMessage(commandFeedback.sendCommandFeedback("not-online"));
                    return Command.SINGLE_SUCCESS;
                }

                sender.sendMessage(commandFeedback.sendCommandFeedback("searching"));

                List<HoneypotPlayerHistoryObject> history = playerHistoryManager.getPlayerHistory(args.player);
                int length = config.config().getInt("history-length");

                if (history.size() > length) {
                    sender.sendMessage(commandFeedback.sendCommandFeedback("truncating"));
                    history = history.subList(0, length);
                }

                if (history.isEmpty()) {
                    sender.sendMessage(commandFeedback.sendCommandFeedback("no-history"));
                    return Command.SINGLE_SUCCESS;
                }

                // Reverse the history array so that it's in chronological order when sent to the player
                Collections.reverse(history);

                int limit = Math.min(history.size(), length);

                for (int i = 0; i < limit; i++) {
                    sender.sendMessage(
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
                                if (!(audience instanceof Player player)) return;
                                player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to teleport").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)));

                    sender.sendMessage(playerInfo);
                    sender.sendMessage(Component.text("Action: ").append(Component.text(history.get(i).getHoneypot().getAction(), NamedTextColor.GOLD)));
                    sender.sendMessage(Component.text("Break type: ").append(Component.text(history.get(i).getType(), NamedTextColor.GOLD)));
                    sender.sendMessage(Component.text("----------------------------------", NamedTextColor.GOLD));
                }
                break;
            case "purge":
                playerHistoryManager.deleteAllHistory();
                sender.sendMessage(commandFeedback.sendCommandFeedback("success"));
                break;
            default:
                sender.sendMessage(commandFeedback.sendCommandFeedback("usage"));
                break;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static HoneypotHistoryArgs parseArgumentsFromContext(CommandContext<CommandSourceStack> ctx) {
        @Nullable String actionArg = null;
        @Nullable Player playerArg = null;
        int countArg = 0;

        try {
            actionArg = StringArgumentType.getString(ctx, "action");
            playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            countArg = IntegerArgumentType.getInteger(ctx, "count");
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            return new HoneypotHistoryArgs(actionArg, playerArg, countArg);
        }

        return new HoneypotHistoryArgs(actionArg, playerArg, countArg);
    }

    private record HoneypotHistoryArgs(@Nullable String action, @Nullable Player player, int count) {
        public boolean isValid() {
            return action != null && player != null;
        } // primitives can't be null, but we don't care if `count` is null or not
    }
}
