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
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.HoneypotCreateEvent;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.store.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import java.util.List;

public class Create implements CommandCallback {

    private final CommandFeedback commandFeedback;
    private final BytePluginConfig config;
    private final HoneypotBlockManager blockManager;
    private final AdapterManager adapterManager;

    @Inject
    Create(CommandFeedback commandFeedback, BytePluginConfig config, HoneypotBlockManager blockManager, AdapterManager adapterManager) {
        this.commandFeedback = commandFeedback;
        this.config = config;
        this.blockManager = blockManager;
        this.adapterManager = adapterManager;
    }

    private boolean isAllowedPerFilters(Block block) {
        List<String> allowedBlocks = config.config().getStringList("allowed-blocks");
        List<String> allowedInventories = config.config().getStringList("allowed-inventories");
        boolean allowed = false;

        if (config.config().getBoolean("filters.blocks")) {
            for (String blockType : allowedBlocks) {
                assert block != null;
                if (block.getType().name().equals(blockType)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (config.config().getBoolean("filters.inventories")) {
            for (String blockType : allowedInventories) {
                if (block.getType().name().equals(blockType)) {
                    allowed = true;
                    break;
                }
            }
        }

        return allowed;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        String typeArg;

        try {
            typeArg = StringArgumentType.getString(ctx, "type");
            if (!config.require("honeypots").contains(typeArg) && Registry.getBehaviorRegistry().getBehaviorProvider(typeArg) == null) {
                ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("no-exist"));
                return Command.SINGLE_SUCCESS;
            }
        } catch (IllegalArgumentException ignored) {
            ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("usage"));
            return Command.SINGLE_SUCCESS;
        }

        Block block;
        WorldGuardAdapter wga = adapterManager.getWorldGuardAdapter();
        GriefPreventionAdapter gpa = adapterManager.getGriefPreventionAdapter();
        LandsAdapter la = adapterManager.getLandsAdapter();

        Player p = (Player) ctx.getSource().getSender(); // This is safe because this command has a requirement that only allows players to execute it

        // Get the block the player is looking at
        if (p.getTargetBlockExact(5) != null) {
            block = p.getTargetBlockExact(5);
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("not-looking-at-block"));
            return Command.SINGLE_SUCCESS;
        }

        if (block == null) return Command.SINGLE_SUCCESS;

        // Check if in a WorldGuard region and the flag is set to deny. If it is, don't
        // bother continuing
        if (wga != null && !wga.isAllowed(p, block.getLocation())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("worldguard"));
            return Command.SINGLE_SUCCESS;
        }

        // Check if in a GriefPrevention region
        if (gpa != null && !gpa.isAllowed(p, block.getLocation())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("griefprevention"));
            return Command.SINGLE_SUCCESS;
        }

        // Check if in a Lands region
        if (la != null && !la.isAllowed(block.getLocation())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("lands"));
            return Command.SINGLE_SUCCESS;
        }

        // Check if the filter is enabled, and if so, if it's allowed
        if ((config.config().getBoolean("filters.blocks")
                || config.config().getBoolean("filters.inventories"))
                && (!isAllowedPerFilters(block))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("against-filter"));
            return Command.SINGLE_SUCCESS;

        }

        // If the block already exists in the DB
        if (blockManager.isHoneypotBlock(block)) {
            p.sendMessage(commandFeedback.sendCommandFeedback("already-exists"));

            // If the block doesn't exist
        } else {

            // Fire HoneypotPreCreateEvent and cancel the command execution if any other plugin cancels the event itself
            HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent(p, block);
            Bukkit.getPluginManager().callEvent(hpce);

            if (hpce.isCancelled())
                return Command.SINGLE_SUCCESS;


            blockManager.createBlock(block, typeArg);
            p.sendMessage(commandFeedback.sendCommandFeedback("success", true));

            // Fire HoneypotCreateEvent
            HoneypotCreateEvent hce = new HoneypotCreateEvent(p, block);
            Bukkit.getPluginManager().callEvent(hce);
        }

        return Command.SINGLE_SUCCESS;
    }
}
