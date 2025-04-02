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

package org.reprogle.honeypot.common.utils;

/**
 * A class used for writing and managing permissions better. This class does not
 * yet have the ability to handle exlusivity, but I'm working on that. I've put
 * the permissions in a class to add features later
 */
public record HoneypotPermission(String permission) {

	/**
	 * Get the string of the permission required
	 *
	 * @return Permission string
	 */
	@Override
	public String permission() {
		return permission;
	}

}
