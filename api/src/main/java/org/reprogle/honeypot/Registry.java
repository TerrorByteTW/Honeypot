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

package org.reprogle.honeypot;

import org.reprogle.honeypot.common.storageproviders.PlayerHistoryStore;
import org.reprogle.honeypot.common.storageproviders.PlayerStore;
import org.reprogle.honeypot.common.storageproviders.RegionStore;

public class Registry {
    private static BehaviorRegistry behaviorRegistry = new BehaviorRegistry();
    private static StoreRegistry storeRegistry = new StoreRegistry();
    private static RegionStore regionStore = null;
    private static PlayerStore playerStore = null;
    private static PlayerHistoryStore playerHistoryStore = null;

    /**
     * Gets the current behavior registry instance.
     * @return The behavior registry.
     */
    public static BehaviorRegistry getBehaviorRegistry() {
        return behaviorRegistry;
    }

    /**
     * Sets the behavior registry instance.
     * @param behaviorRegistry The behavior registry to set.
     */
    public static void setBehaviorRegistry(BehaviorRegistry behaviorRegistry) {
        Registry.behaviorRegistry = behaviorRegistry;
    }

    /**
     * Gets the current storage manager registry instance.
     * @return The storage manager registry.
     */
    public static StoreRegistry getStorageManagerRegistry() {
        return storeRegistry;
    }

    /**
     * Sets the storage manager registry instance.
     * @param storeRegistry The storage manager registry to set.
     */
    public static void setStorageManagerRegistry(StoreRegistry storeRegistry) {
        Registry.storeRegistry = storeRegistry;
    }

    /**
     * Gets the current storage provider instance.
     * @return The storage provider.
     */
    public static RegionStore getRegionStore() {
        return regionStore;
    }

    /**
     * Gets the current player store instance.
     * @return The player store.
     */
    public static PlayerStore getPlayerStore() {
        return playerStore;
    }

    /**
     * Gets the current player history store instance.
     * @return The player history store.
     */
    public static PlayerHistoryStore getPlayerHistoryStore() {
        return playerHistoryStore;
    }

    /**
     * Sets the storage provider instance.
     * @param provider The storage provider to set.
     */
    public static void setRegionStore(RegionStore provider) {
        Registry.regionStore = provider;
    }

    /**
     * Sets the player store instance.
     * @param playerStore The player store to set.
     */
    public static void setPlayerStore(PlayerStore playerStore) {
        Registry.playerStore = playerStore;
    }

    /**
     * Sets the player history store instance.
     * @param playerHistoryStore The player history store to set.
     */
    public static void setPlayerHistoryStore(PlayerHistoryStore playerHistoryStore) {
        Registry.playerHistoryStore = playerHistoryStore;
    }
}
