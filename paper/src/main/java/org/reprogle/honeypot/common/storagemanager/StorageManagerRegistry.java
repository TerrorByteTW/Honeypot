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

package org.reprogle.honeypot.common.storagemanager;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.common.providers.Behavior;
import org.reprogle.honeypot.common.storageproviders.Storage;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;
import org.reprogle.honeypot.common.storageproviders.exceptions.InvalidStorageManagerDefinitionException;
import org.reprogle.honeypot.common.storageproviders.exceptions.StorageManagerConflictException;

import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class StorageManagerRegistry {
    protected final ConcurrentMap<String, StorageProvider> storageProviders = Maps.newConcurrentMap();
    private final Object lock = new Object();
    private boolean initialzed = false;

    public boolean isInitialized() {
        return this.initialzed;
    }

    public void setInitialzed(boolean initialzed) {
        this.initialzed = initialzed;
    }

    public void register(@NotNull StorageProvider storageProvider) {
        synchronized (lock) {
            if (initialzed)
                throw new IllegalStateException("New storage providers cannot be registered at this time");

            try {
                forceRegister(storageProvider);
            } catch (InvalidStorageManagerDefinitionException | StorageManagerConflictException e) {
                Logger.getLogger("minecraft").warning(e.getMessage());
                Logger.getLogger("minecraft").warning("An error occurred while registering a behavior. Please see details above!");
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private StorageProvider forceRegister(StorageProvider provider) throws InvalidStorageManagerDefinitionException, StorageManagerConflictException {
        synchronized (lock) {
            if (!provider.getClass().isAnnotationPresent(Storage.class))
                throw new InvalidStorageManagerDefinitionException("Storage manager " + provider.getClass().getName().toLowerCase() + " is improperly defined, and therefore cannot be registered. Please contact the author of the plugin attempting to register this provider");

            if (storageProviders.containsKey(provider.getProviderName().toLowerCase())) {
                throw new StorageManagerConflictException("Storage manager " + provider.getClass().getName().toLowerCase() + " is already registered under that name. Please rename the Behavior");
            }

            return storageProviders.put(provider.getProviderName().toLowerCase(), provider);
        }
    }

    /**
     * Returns a storage provider based on registered name
     *
     * @param name The name of the provider to pull
     * @return {@link StorageProvider} The storage provider you requested
     */
    public StorageProvider getStorageProvider(@NotNull String name) {
        return storageProviders.get(name.toLowerCase());
    }

    /**
     * Returns all storage providers
     *
     * @return A concurrent map of all storage providers in the form of String, StorageProvider
     */
    public ConcurrentMap<String, StorageProvider> getStorageProviders() {
        return storageProviders;
    }

    /**
     * Get the size of the registry
     *
     * @return An int representing how many Behavior Providers are registered
     */
    public int size() {
        return storageProviders.size();
    }

}
