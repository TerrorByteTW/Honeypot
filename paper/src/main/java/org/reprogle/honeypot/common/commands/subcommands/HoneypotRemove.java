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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.List;

public class HoneypotRemove implements HoneypotSubCommand {

    private final HoneypotConfigManager configManager;
    private final HoneypotBlockManager blockManager;
    private final CommandFeedback commandFeedback;

    @Inject
    public HoneypotRemove(HoneypotConfigManager configManager, HoneypotBlockManager blockManager, CommandFeedback commandFeedback) {
        this.configManager = configManager;
        this.blockManager = blockManager;
        this.commandFeedback = commandFeedback;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    @SuppressWarnings("java:S3776")
    public void perform(Player p, String[] args) {
        Block block = p.getTargetBlockExact(5);

        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "all" -> {
                    blockManager.deleteAllHoneypotBlocks(p.getWorld());
                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted", true));
                }

                case "near" -> {
                    final int radius = configManager.getPluginConfig().getInt("search-range");
                    List<HoneypotBlockObject> honeypots = Registry.getStorageProvider().getNearbyHoneypots(p.getLocation(), radius);

                    if (honeypots.isEmpty()) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                        return;
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
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        if (args.length == 2) {
            // Return all action types for the /honeypot create command
            subcommands.add("all");
            subcommands.add("near");
        }

        return subcommands;
    }

    private void potRemovalCheck(Block block, Player p) {
        assert block != null;
        if (blockManager.isHoneypotBlock(block)) {
            blockManager.deleteBlock(block);
            p.sendMessage(commandFeedback.sendCommandFeedback("success", false));
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("not-a-honeypot"));
        }
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.remove"));
        permissions.add(new HoneypotPermission("honeypot.break"));
        return permissions;
    }
}
