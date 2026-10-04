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

package org.reprogle.honeypot.common.storageproviders;

import org.reprogle.honeypot.api.events.TriggerType;

import java.util.HashMap;
import java.util.UUID;

public class HoneypotPlayerObject {

    private final UUID UUID;

    private final HashMap<TriggerType, Integer> breakTypeCounts;

    /**
     * Create a HoneypotPlayerObject
     *
     * @param uuid         The UUID of the player
     * @param blocksBroken How many blocks the player has broken
     * @deprecated Breaks can now be categorized. Use the constructor that supports HashMaps
     */
    @Deprecated
    public HoneypotPlayerObject(UUID uuid, int blocksBroken) {
        this.UUID = uuid;
        this.breakTypeCounts = new HashMap<>();
        this.breakTypeCounts.put(TriggerType.BREAK, blocksBroken);
    }

    /**
     * Create a HoneypotPlayerObject
     *
     * @param uuid            The UUID of the player
     * @param breakTypeCounts A HashMap of the number of blocks broken by the player, categorized by TriggerType
     */
    public HoneypotPlayerObject(UUID uuid, HashMap<TriggerType, Integer> breakTypeCounts) {
        this.UUID = uuid;
        this.breakTypeCounts = breakTypeCounts;
    }

    /**
     * Get the UUID of the player
     *
     * @return Player's UUID
     */
    public UUID getUUID() {
        return UUID;
    }

    /**
     * Gets the number of blocks broken by the player
     *
     * @return Number of blocks broken
     * @deprecated Breaks can now be categorized. Use {@link getCountByTrigger}. This method will only return block breaks
     */
    @Deprecated
    public int getBlocksBroken() {
        return breakTypeCounts.getOrDefault(TriggerType.BREAK, 0);
    }

    /**
     * Set the number of blocks broken
     *
     * @param blocksBroken The number of blocks the player has broken
     * @deprecated Breaks can now be categorized. Use {@link setCountByTrigger}. This method will only set block breaks
     */
    @Deprecated
    public void setBlocksBroken(int blocksBroken) {
        this.breakTypeCounts.put(TriggerType.BREAK, blocksBroken);
    }

    /**
     * Gets the hashmap of all trigger counts
     * @return The hashmap of all trigger counts for the player
     */
    public HashMap<TriggerType, Integer> getTriggerCounts() {
        return this.breakTypeCounts;
    }

    /**
     * Sets the count for a specific trigger type
     * @param trigger The trigger type to set the count for
     * @param count The count to set for the trigger type
     */
    public void setCountByTrigger(TriggerType trigger, int count) {
        this.breakTypeCounts.put(trigger, count);
    }

    /**
     * Gets the counts for a specific trigger type
     * @param trigger The trigger type to get the count for, or 0 if not found
     * @return The count for the specified trigger type
     */
    public int getCountByTrigger(TriggerType trigger) {
        return breakTypeCounts.getOrDefault(trigger, 0);
    }
}
