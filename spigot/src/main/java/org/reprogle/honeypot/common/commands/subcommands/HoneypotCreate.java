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

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotCreateEvent;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class HoneypotCreate implements HoneypotSubCommand {
	
	private final CommandFeedback commandFeedback;
	private final HoneypotConfigManager configManager;
	private final HoneypotBlockManager blockManager;
	private final AdapterManager adapterManager;
	private final Honeypot plugin;

	@Inject
	HoneypotCreate(CommandFeedback commandFeedback, HoneypotConfigManager configManager, HoneypotBlockManager blockManager, AdapterManager adapterManager, Honeypot plugin) {
		this.commandFeedback = commandFeedback;
		this.configManager = configManager;
		this.blockManager = blockManager;
		this.adapterManager = adapterManager;
		this.plugin = plugin;
	}

	@Override
	public String getName() {
		return "create";
	}

	@Override
	@SuppressWarnings({ "java:S3776", "java:S1192", "java:S6541" })
	public void perform(Player p, String[] args) {
		Block block;
		WorldGuardAdapter wga = adapterManager.getWorldGuardAdapter();
		GriefPreventionAdapter gpa = adapterManager.getGriefPreventionAdapter();
		LandsAdapter la = adapterManager.getLandsAdapter();

		// Get block the player is looking at
		if (p.getTargetBlockExact(5) != null) {
			block = p.getTargetBlockExact(5);
		} else {
			p.sendMessage(commandFeedback.sendCommandFeedback("not-looking-at-block"));
			return;
		}

		// Check if in a WorldGuard region and the flag is set to deny. If it is, don't
		// bother continuing
		if (wga != null && !wga.isAllowed(p, block.getLocation())) {
			p.sendMessage(commandFeedback.sendCommandFeedback("worldguard"));
			return;
		}

		// Check if in a GriefPrevention region
		if (gpa != null && !gpa.isAllowed(p, block.getLocation())) {
			p.sendMessage(commandFeedback.sendCommandFeedback("griefprevention"));
			return;
		}

		// Check if in a Lands region
		if (la != null && !la.isAllowed(block.getLocation())) {
			p.sendMessage(commandFeedback.sendCommandFeedback("lands"));
			return;
		}

		// Check if the filter is enabled, and if so, if it's allowed
		if (configManager.getPluginConfig().getBoolean("filters.blocks")
				|| configManager.getPluginConfig().getBoolean("filters.inventories")
						&& (!isAllowedPerFilters(block))) {
			p.sendMessage(commandFeedback.sendCommandFeedback("against-filter"));
			return;

		}

		// If the block already exists in the DB
		if (blockManager.isHoneypotBlock(block)) {
			p.sendMessage(commandFeedback.sendCommandFeedback("already-exists"));

			// If the block doesn't exist
		} else {
			if (args.length >= 2) {

				// Fire HoneypotPreCreateEvent
				HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent(p, block);
				Bukkit.getPluginManager().callEvent(hpce);

				// Don't do anything if the event is cancelled
				if (hpce.isCancelled())
					return;

				if (args[1].equalsIgnoreCase("custom")) {
					if (!args[2].isEmpty() && configManager.getHoneypotsConfig().contains(args[2])) {
						blockManager.createBlock(block, args[2]);
						p.sendMessage(commandFeedback.sendCommandFeedback("success", true));
					} else {
						p.sendMessage(commandFeedback.sendCommandFeedback("no-exist"));
					}
				} else {
					blockManager.createBlock(block, args[1]);
					p.sendMessage(commandFeedback.sendCommandFeedback("success", true));
				}

				// Fire HoneypotCreateEvent
				HoneypotCreateEvent hce = new HoneypotCreateEvent(p, block);
				Bukkit.getPluginManager().callEvent(hce);

			} else {
				p.sendMessage(commandFeedback.sendCommandFeedback("usage"));
			}
		}
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		List<String> subcommands = new ArrayList<>();

		// We are already in argument 1 of the command, hence why this is a subcommand
		// class. Argument 2 is the
		// subcommand for the subcommand,
		// aka /honeypot create <THIS ONE>

		if (args.length == 2) {
			// Add all behavior providers to the subcommands list, including the built-in
			// ones
			ConcurrentMap<String, BehaviorProvider> map = plugin.getRegistry().getBehaviorProviders();
			map.forEach((providerName, provider) -> subcommands.add(providerName));

			// Add all custom config actions to the subcommands list
			Set<Object> keys = configManager.getHoneypotsConfig().getKeys();
			for (Object key : keys) {
				subcommands.add(key.toString());
			}
		}
		return subcommands;
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.create"));
		return permissions;
	}

	private boolean isAllowedPerFilters(Block block) {
		List<String> allowedBlocks = configManager.getPluginConfig().getStringList("allowed-blocks");
		List<String> allowedInventories = configManager.getPluginConfig().getStringList("allowed-inventories");
		boolean allowed = false;

		if (configManager.getPluginConfig().getBoolean("filters.blocks")) {
			for (String blockType : allowedBlocks) {
				assert block != null;
				if (block.getType().name().equals(blockType)) {
					allowed = true;
					break;
				}
			}
		}

		if (configManager.getPluginConfig().getBoolean("filters.inventories")) {
			for (String blockType : allowedInventories) {
				if (block.getType().name().equals(blockType)) {
					allowed = true;
					break;
				}
			}
		}

		return allowed;
	}
}
