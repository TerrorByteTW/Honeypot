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

package org.reprogle.honeypot.gui.menu;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.gui.GUIMenu;

public class GUIOpenMenu {
    private final GUIMenu gui;

    private final Player player;

    public GUIOpenMenu(GUIMenu gui, Player player) {
        this.gui = gui;
        this.player = player;
    }

    public GUIMenu getGUI() {
        return this.gui;
    }

    public Player getPlayer() {
        return this.player;
    }
}
