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

package org.reprogle.honeypot.common.commands;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.io.IOException;
import java.util.List;

public interface HoneypotSubCommand {

	/**
	 * Gets the name of the command
	 *
	 * @return The String name
	 */
	String getName();

	/**
	 * Performs the command
	 *
	 * @param p    The Player running the command
	 * @param args Any arguments to pass
	 * @throws IOException Throws if any IO actions fail inside the perform command
	 *                     (Such as DB calls)
	 */
	void perform(Player p, String[] args) throws IOException;

	/**
	 * Gets all subcommands of the main command if any (Such as with the create or
	 * remove command)
	 *
	 * @param p    The Player running the command
	 * @param args Any arguments to pass
	 * @return A list of all subcommands as strings
	 */
	List<String> getSubcommands(Player p, String[] args);

	/**
	 * Gets the required permissions to run the command. May be multiple
	 *
	 * @return A list of all subcommands as strings
	 */
	List<HoneypotPermission> getRequiredPermissions();

}
