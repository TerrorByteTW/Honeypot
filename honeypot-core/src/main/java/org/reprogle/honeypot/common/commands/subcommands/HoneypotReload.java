/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
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

import org.bukkit.entity.Player;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HoneypotReload implements HoneypotSubCommand {
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public void perform(Player p, String[] args) {
		try {
			HoneypotConfigManager.getPluginConfig().reload();
			HoneypotConfigManager.getPluginConfig().save();

			HoneypotConfigManager.getGuiConfig().reload();
			HoneypotConfigManager.getGuiConfig().save();

			HoneypotConfigManager.getHoneypotsConfig().reload();
			HoneypotConfigManager.getHoneypotsConfig().save();

			HoneypotConfigManager.getLanguageFile().reload();
			HoneypotConfigManager.getLanguageFile().save();

			p.sendMessage(CommandFeedback.sendCommandFeedback("reload"));
		} catch (IOException e) {
			// Nothing
		}
	}

	// We don't have any subcommands here, but we cannot return null otherwise the
	// tab completer in the CommandManager
	// will throw an exception since CopyPartialMatches doesn't allow null values
	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.reload"));
		return permissions;
	}
}
