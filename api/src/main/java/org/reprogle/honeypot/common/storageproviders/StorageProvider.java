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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public abstract class StorageProvider {
    private final String providerName;

    protected StorageProvider() {
        this.providerName = getClass().getAnnotation(HoneypotStore.class).name();
    }

    public String getProviderName() {
        return providerName;
    }

    /**
     * Override default equals function to provide comparison support to
     * StorageProviders.
     * Since StorageProviders must have unique names, this checks against name only
     *
     * @param o The object which we are checking equality against
     * @return True if the behavior providers are equal to each other
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StorageProvider provider))
            return false;
        if (o == this)
            return true;

        // Don't really care about the type or icon since providerName must be unique
        return provider.getProviderName().equals(this.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(providerName);
    }

    public abstract void createHoneypotBlock(Block block, String action);

    public abstract void removeHoneypotBlock(Block block);

    public abstract boolean isHoneypotBlock(Block block);

    public abstract HoneypotBlockObject getHoneypotBlock(Block block);

    public abstract String getAction(Block block);

    public abstract void deleteAllHoneypotBlocks(@Nullable World world);

    public abstract List<HoneypotBlockObject> getAllHoneypots(@Nullable World world);

    public abstract List<HoneypotBlockObject> getNearbyHoneypots(Location location, int radius);
}
