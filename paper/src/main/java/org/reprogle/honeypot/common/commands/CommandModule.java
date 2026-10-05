/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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

package org.reprogle.honeypot.common.commands;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.ProvidesIntoSet;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.bytelib.commands.dsl.*;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.subcommands.*;

public final class CommandModule extends AbstractModule {

    @ProvidesIntoSet
    CommandRegistration command(BytePluginConfig config, CommandFactory factory) {
        LiteralNode root = CommandDsl.literal("honeypot")
            .requires(
                PermissionChecks.anyOf(
                    PermissionChecks.permission("honeypot.commands"),
                    PermissionChecks.consoleOnly()
                )
            )
            .then(Create.commandTree(config, factory))
            .then(GUI.commandTree(config, factory))
            .then(Help.commandTree())
            .then(History.commandTree(factory))
            .then(Info.commandTree(factory))
            .then(List.commandTree(factory))
            .then(Locate.commandTree(factory))
            .then(Reload.commandTree(factory))
            .then(Remove.commandTree(factory))
            .executes(Help.class, factory);

        return new DslCommandRegistration(root);
    }

}
