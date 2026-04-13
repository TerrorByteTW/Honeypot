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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.HoneypotSupportedVersions;

public class Info implements CommandCallback {

    private final JavaPlugin plugin;
    private final CommandFeedback commandFeedback;
    private final HoneypotSupportedVersions supportedVersions;

    @Inject
    public Info(JavaPlugin plugin, CommandFeedback commandFeedback, HoneypotSupportedVersions supportedVersions) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.supportedVersions = supportedVersions;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        var sender = ctx.getSource().getSender();
        sender.sendMessage(commandFeedback.getChatPrefix()
            .append(Component.text(" "))
            .append(Component.text("Honeypot " + plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(commandFeedback.getChatPrefix()
            .append(Component.text(" "))
            .append(Component.text("Running on " + Bukkit.getServer().getName() + " " + Bukkit.getVersion(), NamedTextColor.WHITE)));
        supportedVersions.checkIfServerSupported();

        return Command.SINGLE_SUCCESS;
    }
}
