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

package org.reprogle.honeypot.common.gui.item;

public enum GUIItemDataColor {

	WHITE((short) 0),
	ORANGE((short) 1),
	MAGENTA((short) 2),
	LIGHT_BLUE((short) 3),
	YELLOW((short) 4),
	LIME((short) 5),
	PINK((short) 6),
	GRAY((short) 7),
	LIGHT_GRAY((short) 8),
	CYAN((short) 9),
	PURPLE((short) 10),
	BLUE((short) 11),
	BROWN((short) 12),
	GREEN((short) 13),
	RED((short) 14),
	BLACK((short) 15);

	/**
	 * The durability value of the color.
	 */
	private final short value;

	GUIItemDataColor(short value) {
		this.value = value;
	}

	public short getValue() {
		return value;
	}

	public static GUIItemDataColor getByValue(short value) {
		for (GUIItemDataColor color : GUIItemDataColor.values()) {
			if (value == color.value)
				return color;
		}

		return null;
	}

}
