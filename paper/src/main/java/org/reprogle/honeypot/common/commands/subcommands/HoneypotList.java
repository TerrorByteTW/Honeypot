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
import org.bukkit.entity.Player;
import org.reprogle.bytelib.commands.dsl.CommandCallback;

public class HoneypotList implements CommandCallback {

	private final HoneypotGUI gui;

	@Inject
	public HoneypotList(HoneypotGUI gui) {
		this.gui = gui;
	}

	@Override
	public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
		// We know this is safe because there is a requirement that the sender is a player
		Player p = (Player) ctx.getSource().getSender();
		gui.callAllHoneypotsInventory(p);
		return Command.SINGLE_SUCCESS;
	}
}
