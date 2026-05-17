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
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.commands.dsl.CommandDsl;
import org.reprogle.bytelib.commands.dsl.LiteralNode;
import org.reprogle.bytelib.commands.dsl.PermissionChecks;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storageproviders.PlayerHistoryStore;
import org.reprogle.honeypot.common.storageproviders.PlayerStore;
import org.reprogle.honeypot.common.storageproviders.RegionStore;
import org.reprogle.honeypot.common.utils.GhostHoneypotMonitor;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.Optional;

public class Reload implements CommandCallback {

    private final BytePluginConfig config;
    private final GhostHoneypotMonitor monitor;
    private final CommandFeedback commandFeedback;
    private final HoneypotLogger logger;

    @Inject
    public Reload(BytePluginConfig config, GhostHoneypotMonitor monitor, CommandFeedback commandFeedback, HoneypotLogger logger) {
        this.config = config;
        this.monitor = monitor;
        this.commandFeedback = commandFeedback;
        this.logger = logger;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        config.reload();

        monitor.cancelTask();
        if (config.config().getBoolean("ghost-honeypot-checker.enable")) {
            monitor.startTask();
        }

        String regionStore = config.config().getString("storage-method.regions");
        String playerStore = config.config().getString("storage-method.players");
        String playerHistoryStore = config.config().getString("storage-method.player-history");

        if (!Registry.getRegionStore().getProviderName().equalsIgnoreCase(regionStore)) {
            Optional<RegionStore> provider = Registry.getStorageManagerRegistry().get(regionStore, RegionStore.class);
            if (provider.isPresent()) {
                Registry.setRegionStore(provider.get());
                logger.info(Component.text("The region store was updated to \"" + regionStore + "\""));
            } else {
                logger.severe(Component.text("The region store was updated to \"" + regionStore + "\" but it is not registered! Honeypot will continue to use the previously set provider, but on your next reboot Honeypot WILL crash ON PURPOSE until fixed! Please validate your config"));
            }
        }

        if (!Registry.getPlayerStore().getProviderName().equalsIgnoreCase(playerStore)) {
            Optional<PlayerStore> provider = Registry.getStorageManagerRegistry().get(playerStore, PlayerStore.class);
            if (provider.isPresent()) {
                Registry.setPlayerStore(provider.get());
                logger.info(Component.text("The player store was updated to \"" + playerStore + "\""));
            } else {
                logger.severe(Component.text("The player store was updated to \"" + playerStore + "\" but it is not registered! Honeypot will continue to use the previously set provider, but on your next reboot Honeypot WILL crash ON PURPOSE until fixed! Please validate your config"));
            }
        }

        if (!Registry.getPlayerHistoryStore().getProviderName().equalsIgnoreCase(playerHistoryStore)) {
            Optional<PlayerHistoryStore> provider = Registry.getStorageManagerRegistry().get(playerHistoryStore, PlayerHistoryStore.class);
            if (provider.isPresent()) {
                Registry.setPlayerHistoryStore(provider.get());
                logger.info(Component.text("The player history store was updated to \"" + playerHistoryStore + "\""));
            } else {
                logger.severe(Component.text("The player history store was updated to \"" + playerHistoryStore + "\" but it is not registered! Honeypot will continue to use the previously set provider, but on your next reboot Honeypot WILL crash ON PURPOSE until fixed! Please validate your config"));
            }
        }

        ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("reload"));
        logger.info(Component.text("Honeypot has successfully been reloaded"));

        return 0;
    }

    public static LiteralNode commandTree(CommandFactory factory) {
        return CommandDsl.literal("reload")
            .requires(
                PermissionChecks.anyOf(
                    PermissionChecks.permission("honeypot.reload"),
                    PermissionChecks.permission("honeypot.*"),
                    PermissionChecks.isOp()
                )
            )
            .executes(Reload.class, factory);
    }
}
