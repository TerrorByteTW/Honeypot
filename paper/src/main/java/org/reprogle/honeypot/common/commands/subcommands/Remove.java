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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.dsl.*;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;

import java.util.List;

public class Remove implements CommandCallback {

    private final BytePluginConfig config;
    private final HoneypotRegionManager regionManager;
    private final CommandFeedback commandFeedback;

    @Inject
    public Remove(BytePluginConfig config, HoneypotRegionManager regionManager, CommandFeedback commandFeedback) {
        this.config = config;
        this.regionManager = regionManager;
        this.commandFeedback = commandFeedback;
    }

    private void potRemovalCheck(Block block, Player p) {
        if (block != null && regionManager.isHoneypotBlock(block)) {
            regionManager.deleteRegionContaining(block);
            p.sendMessage(commandFeedback.sendCommandFeedback("success.removed"));
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("not-a-honeypot"));
        }
    }


    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        String qualifierArg = null;

        try {
            qualifierArg = StringArgumentType.getString(ctx, "qualifier");
        } catch (IllegalArgumentException ignored) {
        }

        var p = (Player) ctx.getSource().getSender(); // This is safe because this command has a requirement that only allows players to execute it

        Block block = p.getTargetBlockExact(5);

        if (qualifierArg != null) {
            switch (qualifierArg) {
                case "all" -> {
                    regionManager.deleteAllHoneypotBlocks();
                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted.all"));
                }

                case "near" -> {
                    final int radius = config.config().getInt("search-range");
                    List<HoneypotRegionObject> honeypots = Registry.getRegionStore().getNearbyHoneypotRegions(p.getLocation(), radius);

                    if (honeypots.isEmpty()) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                        return Command.SINGLE_SUCCESS;
                    }

                    for (HoneypotRegionObject honeypot : honeypots) {
                        regionManager.deleteRegionContaining(honeypot.getPos1().getBlock());
                    }

                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted.near"));
                }

                default -> potRemovalCheck(block, p);
            }
        } else {
            potRemovalCheck(block, p);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static LiteralNode commandTree(CommandFactory factory) {
        return CommandDsl.literal("remove")
            .requires(
                PermissionChecks.allOf(
                    PermissionChecks.anyOf(
                        PermissionChecks.permission("honeypot.remove"),
                        PermissionChecks.permission("honeypot.*"),
                        PermissionChecks.isOp()
                    ),
                    PermissionChecks.playerOnly()
                )
            )
            .then(
                CommandDsl.argument("qualifier", StringArgumentType.string())
                    .suggests(Suggest.fixed("all", "near"))
            )
            .executes(Remove.class, factory);
    }
}
