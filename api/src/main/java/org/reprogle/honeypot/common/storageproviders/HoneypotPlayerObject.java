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

import java.util.UUID;

public class HoneypotPlayerObject {

	private final UUID UUID;

	private int blocksBroken;

	/**
	 * Create a HoneypotPlayerObject
	 *
	 * @param uuid         The UUID of the player
	 * @param blocksBroken How many blocks the player has broken
	 */
	public HoneypotPlayerObject(UUID uuid, int blocksBroken) {
		this.UUID = uuid;
		this.blocksBroken = blocksBroken;
	}

	/**
	 * Get the UUID of the player
	 *
	 * @return Player's UUID
	 */
	public UUID getUUID() {
		return UUID;
	}

	/**
	 * Get's the number of blocks broken by the player
	 *
	 * @return Amount of blocks broken
	 */
	public int getBlocksBroken() {
		return blocksBroken;
	}

	/**
	 * Set's the amount of blocks broken
	 *
	 * @param blocksBroken The number of blocks the player has broken
	 */
	public void setBlocksBroken(int blocksBroken) {
		this.blocksBroken = blocksBroken;
	}

}
