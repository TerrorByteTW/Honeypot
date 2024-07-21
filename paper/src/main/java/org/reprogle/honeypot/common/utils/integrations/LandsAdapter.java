/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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

package org.reprogle.honeypot.common.utils.integrations;

import org.bukkit.Location;
import org.reprogle.honeypot.Honeypot;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.land.LandWorld;

public class LandsAdapter {

    private final LandsIntegration api;

    /**
     * Construct the LandsIntegration. This constructor is because the LandsIntegration interface must be obtained prior
     * to utilizing the API
     */
    public LandsAdapter(Honeypot plugin) {
        api = LandsIntegration.of(plugin);
    }

    /**
     * Checks if a Honeypot is allowed to be placed at the location
     * @param location The location of the block
     * @return True if the action is allowed (No lands claim surrounding it), false if not OR if the land is
     * unclaimed/lands is not integrated.
     */
    public boolean isAllowed(Location location) {
        LandWorld world = api.getWorld(location.getWorld());
        if (world == null)
            return true;

        // This should *always* be acceptable, because in order for this method to be called,
        // that means a block must have been interacted with, which requires a loaded chunk.
        // HOWEVER, if, for whatever reason, this is *not* acceptable, then #getLandByUnloadedChunk(x, y) is also a
        // valid option
        Land land = world.getLandByChunk(location.getChunk().getX(), location.getChunk().getZ());

        // Land is null if the chunk isn't claimed or isn't loaded. However, as stated, logically the chunk should
        // always be loaded if this method is called, so this should be fine.
        return land == null;

    }

}
