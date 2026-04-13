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
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.honeypot.common.commands.CommandFeedback;

public class Help implements CommandCallback {

	private final CommandFeedback commandFeedback;

	@Inject
    Help(CommandFeedback commandFeedback) {
		this.commandFeedback = commandFeedback;
	}

	@Override
	public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
		ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("usage"));
		return Command.SINGLE_SUCCESS;
	}
}
