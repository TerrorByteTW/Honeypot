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

package org.reprogle.honeypot.common.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * A synthetic {@link BlockBreakEvent} fired when a player blows up a Honeypot (e.g., with TNT they lit). It is handled by
 * every BlockBreakEvent listener like a normal break, but lets {@link BlockBreakEventListener} know that whether the
 * Honeypot survives is decided by {@code allow-explode}, not {@code allow-player-destruction}.
 */
public class ExplosionBlockBreakEvent extends BlockBreakEvent {
    public ExplosionBlockBreakEvent(Block block, Player player) {
        super(block, player);
    }
}
