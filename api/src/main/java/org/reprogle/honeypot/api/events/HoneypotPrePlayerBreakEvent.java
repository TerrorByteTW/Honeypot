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
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered before a Honeypot is broken.
 * This event is called <i>before</i> the block is broken, not after.
 * This event is cancellable. If cancelled, the Honeypot is broken as if it was a regular block.
 */
public class HoneypotPrePlayerBreakEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean isCancelled;

    private final Player player;

    private final Block block;

    /**
     * Called before action is taken on a player who broke with Honeypot inventory. If cancelled, the Honeypot is
     * ignored
     *
     * @param player The Player breaking with the Honeypot
     * @param block  The Honeypot block
     */
    public HoneypotPrePlayerBreakEvent(Player player, Block block) {
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

    /**
     * Check if the event is cancelled
     *
     * @return True if cancelled, false if not
     */
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Set the event as cancelled or not
     *
     * @param cancel Boolean value notating if the event is cancelled or not
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

}