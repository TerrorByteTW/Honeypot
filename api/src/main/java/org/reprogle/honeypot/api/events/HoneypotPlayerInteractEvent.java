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

package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered each time a Honeypot inventory is interacted with.
 * This event is called <i>after</i> the interaction is triggered, not before.
 * This event is not cancellable. If you need to cancel it, use {@link HoneypotPrePlayerInteractEvent}.
 */
public class HoneypotPlayerInteractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;

    private final Block block;

    /**
     * Called after action is taken on a player who interacted with the Honeypot. Non-cancellable
     *
     * @param player The Player who broke with the Honeypot
     * @param block  The Honeypot block
     */
    public HoneypotPlayerInteractEvent(Player player, Block block) {
        this.player = player;
        this.block = block;
    }

    /**
     * Boilerplate function for Bukkit
     *
     * @return HandlerList
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Boilerplate function for Bukkit
     *
     * @return HandlerList
     */
    @SuppressWarnings("java:S4144")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the player that broke the block
     *
     * @return {@link Player}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the block that was involved in the event
     *
     * @return {@link Block}
     */
    public Block getBlock() {
        return block;
    }

}
