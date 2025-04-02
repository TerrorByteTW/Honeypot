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
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is triggered after a Honeypot is edited by a non-player object.
 * This event is called <i>after</i> the block edit, not before.
 * This event is not cancellable, you cannot cancel the editing of a Honeypot moved by a non-object.
 * Currently, this class can only return the Honeypot {@link Block} and a generic Object representing
 * the thing that attempted to edit the block.
 */
public class HoneypotNonPlayerBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Object object;

    private final Block block;

    /**
     * Constructor for event
     *
     * @param object The object doing the breaking
     * @param block  The block being broken
     */
    public HoneypotNonPlayerBreakEvent(Object object, Block block) {
        this.object = object;
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
     * Get the object that triggered the event
     *
     * @return Object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Get the block that was broken
     *
     * @return {@link Block}
     */
    public Block getBlock() {
        return block;
    }
}
