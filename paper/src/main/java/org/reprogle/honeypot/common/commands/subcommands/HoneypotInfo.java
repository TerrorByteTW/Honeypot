/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.commands.subcommands;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class HoneypotInfo implements HoneypotSubCommand {

	private final Honeypot plugin;
	private final CommandFeedback commandFeedback;

	@Inject
	public HoneypotInfo(Honeypot plugin, CommandFeedback commandFeedback) {
		this.plugin = plugin;
		this.commandFeedback = commandFeedback;
	}

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public void perform(Player p, String[] args) {
		p.sendMessage(
				commandFeedback.getChatPrefix() + " Honeypot version " + plugin.getDescription().getVersion());

		p.sendMessage(commandFeedback.getChatPrefix() + " Running on " + Bukkit.getVersion());
		plugin.checkIfServerSupported();
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		return new ArrayList<>();
	}

}
