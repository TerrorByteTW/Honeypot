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

package org.reprogle.honeypot.common.gui.pagination;

public enum GUIPageButtonType {

	PREV_BUTTON(3),
	CURRENT_BUTTON(4),
	NEXT_BUTTON(5),
	UNASSIGNED(0);

	private final int slot;

	GUIPageButtonType(int slot) {
		this.slot = slot;
	}

	public int getSlot() {
		return slot;
	}

	public static GUIPageButtonType forSlot(int slot) {
		for (GUIPageButtonType buttonType : GUIPageButtonType.values()) {
			if (buttonType.slot == slot)
				return buttonType;
		}

		return GUIPageButtonType.UNASSIGNED;
	}

}
