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

import org.reprogle.honeypot.common.storageproviders.StorageProvider;

public class Registry {
    private static BehaviorRegistry behaviorRegistry = new BehaviorRegistry();
    private static HoneypotStoreRegistry honeypotStoreRegistry = new HoneypotStoreRegistry();
    private static StorageProvider provider = null;

    public static BehaviorRegistry getBehaviorRegistry() {
        return behaviorRegistry;
    }

    public static void setBehaviorRegistry(BehaviorRegistry behaviorRegistry) {
        Registry.behaviorRegistry = behaviorRegistry;
    }

    public static HoneypotStoreRegistry getStorageManagerRegistry() {
        return honeypotStoreRegistry;
    }

    public static void setStorageManagerRegistry(HoneypotStoreRegistry honeypotStoreRegistry) {
        Registry.honeypotStoreRegistry = honeypotStoreRegistry;
    }

    public static StorageProvider getStorageProvider() {
        return provider;
    }

    public static void setStorageProvider(StorageProvider provider) {
        Registry.provider = provider;
    }
}
