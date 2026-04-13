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
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.store.HoneypotBlockManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;

import java.util.List;

public class Remove implements CommandCallback {

    private final BytePluginConfig config;
    private final HoneypotBlockManager blockManager;
    private final CommandFeedback commandFeedback;

    @Inject
    public Remove(BytePluginConfig config, HoneypotBlockManager blockManager, CommandFeedback commandFeedback) {
        this.config = config;
        this.blockManager = blockManager;
        this.commandFeedback = commandFeedback;
    }

    private void potRemovalCheck(Block block, Player p) {
        if (block != null && blockManager.isHoneypotBlock(block)) {
            blockManager.deleteBlock(block);
            p.sendMessage(commandFeedback.sendCommandFeedback("success", false));
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
                    blockManager.deleteAllHoneypotBlocks(p.getWorld());
                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted", true));
                }

                case "near" -> {
                    final int radius = config.config().getInt("search-range");
                    List<HoneypotBlockObject> honeypots = Registry.getStorageProvider().getNearbyHoneypots(p.getLocation(), radius);

                    if (honeypots.isEmpty()) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                        return Command.SINGLE_SUCCESS;
                    }

                    for (HoneypotBlockObject honeypot : honeypots) {
                        blockManager.deleteBlock(honeypot.getBlock());
                    }

                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted", false));
                }

                default -> potRemovalCheck(block, p);
            }
        } else {
            potRemovalCheck(block, p);
        }

        return Command.SINGLE_SUCCESS;
    }
}
