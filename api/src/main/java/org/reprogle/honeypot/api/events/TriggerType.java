/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022-2026 TerrorByte and Honeypot Contributors.
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

public enum TriggerType {
    /**
     * Triggers involving the player breaking a block
     */
    BREAK,
    /**
     * Triggers involving the player opening an inventory. Only applicable if the server has `use-inventory-click` set to false
     */
    INVENTORY_OPEN,
    /**
     * Triggers involving the player interacting with an inventory. Only applicable if the server has `use-inventory-click` set to true
     */
    INVENTORY_INTERACT,
    /**
     * Triggers involving the player performing an action that is not covered by the other trigger types, such as placing TNT
     */
    GENERIC,
    /**
     * Triggers involving non-player entities, such as enderman or pistons. Does not actually track in the database, and is only used for events
     */
    NON_PLAYER
}
