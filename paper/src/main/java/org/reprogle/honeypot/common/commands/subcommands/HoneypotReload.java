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
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;
import org.reprogle.honeypot.common.utils.GhostHoneypotFixer;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class HoneypotReload implements CommandCallback {

    private final BytePluginConfig config;
    private final GhostHoneypotFixer fixer;
    private final CommandFeedback commandFeedback;
    private final HoneypotLogger logger;

    @Inject
    public HoneypotReload(BytePluginConfig config, GhostHoneypotFixer fixer, CommandFeedback commandFeedback, HoneypotLogger logger) {
        this.config = config;
        this.fixer = fixer;
        this.commandFeedback = commandFeedback;
        this.logger = logger;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        config.reload();

        fixer.cancelTask();
        if (config.config().getBoolean("ghost-honeypot-checker.enable")) {
            fixer.startTask();
        }

        String providerName = config.config().getString("storage-method");
        if (!Registry.getStorageProvider().getProviderName().equalsIgnoreCase(providerName)) {
            StorageProvider provider = Registry.getStorageManagerRegistry().getStorageProvider(providerName);
            if (provider != null) {
                Registry.setStorageProvider(provider);
                logger.info(Component.text("The storage provider was updated to \"" + providerName + "\""));
            } else {
                logger.severe(Component.text("The storage provider was updated to \"" + providerName + "\" but it is not registered! Honeypot will continue to use the previously set provider, but on your next reboot Honeypot WILL crash ON PURPOSE! Please validate your config"));
            }
        }

        ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("reload"));
        logger.info(Component.text("Honeypot has successfully been reloaded"));

        return 0;
    }
}
