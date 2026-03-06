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
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;

public class HoneypotMigrate implements CommandCallback {

    private final CommandFeedback commandFeedback;
    private final BytePluginConfig config;
    private final HoneypotBlockManager hbm;
    private final JavaPlugin plugin;
    private final DataStoreManager dataStoreManager;
    private final HoneypotLogger logger;

    @Inject
    public HoneypotMigrate(CommandFeedback commandFeedback, BytePluginConfig config, HoneypotBlockManager hbm, JavaPlugin plugin, DataStoreManager dataStoreManager, HoneypotLogger logger) {
        this.commandFeedback = commandFeedback;
        this.config = config;
        this.hbm = hbm;
        this.plugin = plugin;
        this.dataStoreManager = dataStoreManager;
        this.logger = logger;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        boolean confirm = false;

        try {
            confirm = ctx.getArgument("confirm", boolean.class);
        } catch (IllegalArgumentException ignored) {
            // a lack of a value implies false
        }

        var sender = ctx.getSource().getSender();

        if (config.config().getString("storage-method").equalsIgnoreCase("pdc")) {
            sender.sendMessage(commandFeedback.sendCommandFeedback("migrate", false));
            return Command.SINGLE_SUCCESS;
        }

        if (!confirm) {
            sender.sendMessage(commandFeedback.sendCommandFeedback("migrate"));
            return Command.SINGLE_SUCCESS;
        }

        logger.severe(commandFeedback.getChatPrefix()
                .append(Component.text(sender.getName() + " has started migrating Honeypot to PDC!!! "))
                .append(Component.text("They were warned that this will cause Honeypot to shutdown afterwards. If you heavily rely on Honeypot, PLEASE restart ASAP!")));

        // Get all worlds in the server, since PDC works on a per-world basis
        List<World> worlds = plugin.getServer().getWorlds();

        // For every world on the server, get all blocks. Add each of those blocks to PDc
        for (World world : worlds) {
            List<HoneypotBlockObject> blocks = hbm.getAllHoneypots(world);
            for (HoneypotBlockObject block : blocks) {
                dataStoreManager.createHoneypotBlock(block.getBlock(), block.getAction());
            }
        }

        sender.sendMessage(commandFeedback.sendCommandFeedback("migrate", true));

        // Change the storage method to pdc and shutdown the plugin to prevent potential issues. The storage method is decided on server start,
        // and absolutely cannot be changed in runtime due to it being injected everywhere via Guice.
        config.config().set("storage-method", "pdc");
        config.config().save();
        plugin.getServer().getPluginManager().disablePlugin(plugin);

        return Command.SINGLE_SUCCESS;
    }
}
