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

import com.google.common.base.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class HoneypotRegionObject {

    private final Location pos1;
    private final Location pos2;
    private final String world;
    private final String action;

    /**
     * Create a HoneypotRegionObject from a single block
     *
     * @param block  The Block of the Honeypot that acts as the entire region
     * @param action The action of the Honeypot
     */
    public HoneypotRegionObject(Block block, String action) {
        this.pos1 = block.getLocation();
        this.pos2 = block.getLocation();
        this.world = block.getWorld().getName();
        this.action = action;
    }

    /**
     * Used for GUI, create a Honeypot Region based off of Positions and not Block objects
     *
     * @param pos1      The first position of the Honeypot Region
     * @param pos2      The second position of the Honeypot Region
     * @param action    The action of the Honeypot
     */
    public HoneypotRegionObject(Location pos1, Location pos2, String action) {
        if (pos1.getWorld() != pos2.getWorld())
            throw new IllegalArgumentException("Positions must be in the same world");
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.world = pos1.getWorld().getName();
        this.action = action;
    }

    /**
     * Used to create a Honeypot Region based off a single position (Single block region)
     *
     * @param location  The first position of the Honeypot Region
     * @param action    The action of the Honeypot
     */
    public HoneypotRegionObject(Location location, String action) {
        this.pos1 = location;
        this.pos2 = location;
        this.world = location.getWorld().getName();
        this.action = action;
    }

    /**
     * Create a Honeypot region based off of individual coordinates for the corners
     *
     * @param worldName The world the block is in
     * @param x1        The x coordinate of the first corner of the region
     * @param y1        The y coordinate of the first corner of the region
     * @param z1        The z coordinate of the first corner of the region
     * @param x2        The x coordinate of the second corner of the region
     * @param y2        The y coordinate of the second corner of the region
     * @param z2        The z coordinate of the second corner of the region
     * @param action    The action of the Honeypot
     */
    public HoneypotRegionObject(String worldName, int x1, int y1, int z1, int x2, int y2, int z2, String action) {
        World world = Bukkit.getWorld(worldName);

        this.pos1 = new Location(world, x1, y1, z1);
        this.pos2 = new Location(world, x2, y2, z2);
        this.world = worldName;
        this.action = action;
    }

    /**
     * Used for Database, create a Honeypot region based off of a single set of coordinates
     *
     * @param worldName The world the block is in
     * @param x         The x coordinate of the region
     * @param y         The y coordinate of the region
     * @param z         The z coordinate of the region
     * @param action    The action of the Honeypot
     */
    public HoneypotRegionObject(String worldName, int x, int y, int z, String action) {
        World world = Bukkit.getWorld(worldName);
        Location loc = new Location(world, x, y, z);

        this.pos1 = loc;
        this.pos2 = loc;
        this.world = worldName;
        this.action = action;
    }

    /**
     * Get the first position of the Honeypot Region
     *
     * @return Location
     */
    public Location getPos1() {
        return pos1;
    }

    /**
     * Get the second position of the Honeypot Region
     *
     * @return Location
     */
    public Location getPos2() {
        return pos2;
    }

    /**
     * Determines whether the Honeypot Region comprises a single block
     * @return True if the region is a single block, false otherwise
     */
    public boolean isSingleBlockRegion() {
        return pos1.equals(pos2);
    }

    /**
     * Get the action of the Honeypot
     *
     * @return action
     */
    public String getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HoneypotRegionObject honeypot))
            return false;
        if (o == this)
            return true;

        // Don't really care about the action since action doesn't determine a Honeypot
        int x1 = pos1.getBlockX(), y1 = pos1.getBlockY(), z1 = pos1.getBlockZ();
        int thisX1 = this.pos1.getBlockX(), thisY1 = this.pos1.getBlockY(), thisZ1 = this.pos1.getBlockZ();

        int x2 = pos2.getBlockX(), y2 = pos2.getBlockY(), z2 = pos2.getBlockZ();
        int thisX2 = this.pos2.getBlockX(), thisY2 = this.pos2.getBlockY(), thisZ2 = this.pos2.getBlockZ();

        return x1 == thisX1 && y1 == thisY1 && z1 == thisZ1 &&
            x2 == thisX2 && y2 == thisY2 && z2 == thisZ2 &&
            honeypot.world.equals(this.world);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos1, pos2, world);
    }

}
