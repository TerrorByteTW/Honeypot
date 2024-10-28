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

package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered each time a Honeypot is created.
 * This event is called <i>after</i> the event is triggered, not before.
 * This event is not cancellable, if you need to cancel it use {@link HoneypotPreCreateEvent}.
 */
public class HoneypotCreateEvent extends Event {

    /**
     * The handlers list of the event.
     *
     * @see HandlerList
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The {@link Player} who fires the event
     */
    private final Player player;

    /**
     * The {@link Block} involved the event
     */
    private final Block block;

    /**
     * Creates a Honeypot Event. This shouldn't be used by other plugins. Instead, listen for the event instead.
     *
     * @param player The {@link Player} who fired the event
     * @param block  The {@link Block} involved in the event
     * @see #getPlayer()
     * @see #getBlock()
     */
    public HoneypotCreateEvent(Player player, Block block) {
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
     * Get the player object of the player who fired the event
     *
     * @return {@link Player}
     * @see #getBlock() #getBlock()
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the block object of the block who fired the event
     *
     * @return {@link Block}
     * @see #getPlayer() #getPlayer()
     */
    public Block getBlock() {
        return block;
    }
}
