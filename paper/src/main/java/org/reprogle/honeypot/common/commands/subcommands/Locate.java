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
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.commands.dsl.CommandDsl;
import org.reprogle.bytelib.commands.dsl.LiteralNode;
import org.reprogle.bytelib.commands.dsl.PermissionChecks;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
import org.reprogle.honeypot.common.utils.RegionOutliner;

import java.util.List;

public class Locate implements CommandCallback {

    private final JavaPlugin plugin;
    private final BytePluginConfig config;
    private final CommandFeedback commandFeedback;

    @Inject
    public Locate(JavaPlugin plugin, BytePluginConfig config, CommandFeedback commandFeedback) {
        this.plugin = plugin;
        this.config = config;
        this.commandFeedback = commandFeedback;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        int radiusArg = config.config().getInt("search-range", 5);

        try {
            radiusArg = IntegerArgumentType.getInteger(ctx, "radius");
        } catch (IllegalArgumentException ignored) {
        }

        Player p = (Player) ctx.getSource().getSender(); // This is safe because there is a requirement that the sender must be a player

        boolean potFound = false;

        List<HoneypotRegionObject> honeypots = Registry.getRegionStore().getNearbyHoneypotRegions(p.getLocation(), radiusArg);
        if (!honeypots.isEmpty()) potFound = true;

        for (HoneypotRegionObject honeypot : honeypots) {
            if (honeypot.isSingleBlockRegion()) {
                RegionOutliner.spawnSlime(plugin, p.getWorld(), honeypot.getPos1().getX(), honeypot.getPos1().getY(), honeypot.getPos1().getZ());
            } else {
                RegionOutliner.outlineBoundingBox(plugin, p, honeypot.getPos1(), honeypot.getPos2());
            }
        }

        // Let the player know if a pot was found or not
        if (potFound) {
            p.sendMessage(commandFeedback.sendCommandFeedback("found-pots"));
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static LiteralNode commandTree(CommandFactory factory) {
        return CommandDsl.literal("locate")
            .requires(
                PermissionChecks.allOf(
                    PermissionChecks.anyOf(
                        PermissionChecks.permission("honeypot.locate"),
                        PermissionChecks.permission("honeypot.*"),
                        PermissionChecks.isOp()
                    ),
                    PermissionChecks.playerOnly()
                )
            )
            .then(
                CommandDsl.argument("radius", IntegerArgumentType.integer(1))
            )
            .executes(Locate.class, factory);
    }
}
