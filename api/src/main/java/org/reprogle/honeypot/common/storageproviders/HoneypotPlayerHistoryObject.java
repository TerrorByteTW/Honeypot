/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright TerrorByte & Honeypot Contributors (c) 2022 - 2024
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

package org.reprogle.honeypot.common.storageproviders;

import org.bukkit.entity.Player;

/**
 * A class representing a player history entry.
 * Includes methods for getting all values of a Honeypot history entry, which can be returned via the HoneypotPlayerHistoryManager class
 */
@SuppressWarnings({"java:S116", "unused"})
public class HoneypotPlayerHistoryObject {
	private final String dateTime;
	private final String player;
	private final String UUID;
	private final HoneypotBlockObject hbo;
	private final String type;

	/**
	 * Constructor for creating a history entry
	 *
	 * @param dateTime The Date and Time in string format. Really need to improve this to have standards but oh well
	 * @param player   The player's name
	 * @param UUID     The UUID of the player
	 * @param hbo      The HoneypotBlockObject they broke
	 */
	public HoneypotPlayerHistoryObject(String dateTime, String player, String UUID, HoneypotBlockObject hbo, String type) {
		this.dateTime = dateTime;
		this.player = player;
		this.UUID = UUID;
		this.hbo = hbo;
		this.type = type;
	}

	/**
	 * Constructor for creating a history entry
	 *
	 * @param dateTime The Date and Time in string format. Really need to improve this to have standards but oh well
	 * @param player   The player object
	 * @param hbo      The HoneypotBlockObject they broke
	 */
	public HoneypotPlayerHistoryObject(String dateTime, Player player, HoneypotBlockObject hbo, String type) {
		this.dateTime = dateTime;
		this.player = player.getName();
		this.UUID = player.getUniqueId().toString();
		this.hbo = hbo;
		this.type = type;
	}

	/**
	 * Get the date and time of the history entry
	 *
	 * @return Date and Time, string formatted and in GMT +0:00
	 */
	public String getDateTime() {
		return dateTime;
	}

	/**
	 * Get the player's name
	 *
	 * @return Player name
	 */
	public String getPlayer() {
		return player;
	}

	/**
	 * Get the player's UUID
	 *
	 * @return UUID
	 */
	public String getUUID() {
		return UUID;
	}

	/**
	 * Get the Honeypot they broke
	 *
	 * @return {@link HoneypotBlockObject}
	 */
	public HoneypotBlockObject getHoneypot() {
		return hbo;
	}

	/**
	 * Get the type of break
	 */
	public String getType() { return type; }


}
