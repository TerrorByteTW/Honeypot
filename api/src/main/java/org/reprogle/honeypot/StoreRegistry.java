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

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.common.storageproviders.*;
import org.reprogle.honeypot.common.storageproviders.exceptions.InvalidStorageManagerDefinitionException;
import org.reprogle.honeypot.common.storageproviders.exceptions.StorageManagerConflictException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class StoreRegistry {

    protected final ConcurrentMap<String, Object> stores = Maps.newConcurrentMap();
    protected final ConcurrentMap<StoreType, Set<String>> storeTypes = Maps.newConcurrentMap();

    private final Object lock = new Object();
    private boolean initialized = false;

    public boolean isInitialized() {
        return this.initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Registers a new storage provider.
     * @param store The store to register. Must be one of {@link PlayerStore}, {@link RegionStore}, or {@link PlayerHistoryStore}.
     */
    public void register(@NotNull Object store) {
        synchronized (lock) {
            if (initialized)
                throw new IllegalStateException("New storage providers cannot be registered at this time");

            try {
                forceRegister(store);
            } catch (InvalidStorageManagerDefinitionException | StorageManagerConflictException e) {
                Logger.getLogger("minecraft").warning(e.getMessage());
                Logger.getLogger("minecraft").warning("An error occurred while registering a behavior. Please see details above!");
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private void forceRegister(Object store) throws InvalidStorageManagerDefinitionException, StorageManagerConflictException {
        synchronized (lock) {
            Class<?> clazz = store.getClass();

            HoneypotStore annotation = clazz.getAnnotation(HoneypotStore.class);

            if (annotation == null)
                throw new InvalidStorageManagerDefinitionException("Storage manager in class " + clazz.getName().toLowerCase() + " is improperly defined, and therefore cannot be registered. Please contact the author of the plugin attempting to register this provider");

            String name = annotation.name();

            Object existing = stores.putIfAbsent(name, store);

            if (existing != null) {
                throw new StorageManagerConflictException("Storage manager " + name + " is already registered. Please use a different name for this provider");
            }

            for (StoreType type : annotation.type()) {
                storeTypes.computeIfAbsent(type, ignored -> ConcurrentHashMap.newKeySet()).add(name);
            }
        }
    }

    /**
     * Get a specific store by name.
     * @param name The name of the store to retrieve.
     * @return An Optional containing the store if found, or empty if not found.
     */
    public Optional<Object> get(String name) {
        return Optional.ofNullable(stores.get(name));
    }

    /**
     * Get a specific store by name and type.
     * @param name The name of the store to retrieve.
     * @param type The expected type of the store.
     * @param <T> The type parameter for the expected store type.
     * @return An Optional containing the store if found and of the expected type, or empty otherwise.
     */
    public <T> Optional<T> get(String name, Class<T> type) {
        return Optional.ofNullable(stores.get(name))
            .filter(type::isInstance)
            .map(type::cast);
    }

    /**
     * Get a list of stores by type and expected type.
     * @param type The type of stores to retrieve.
     * @param expectedType The expected type of the stores.
     * @param <T> The type parameter for the expected store type.
     * @return A list of stores that match the type and expected type criteria.
     */
    public <T> List<T> getByType(StoreType type, Class<T> expectedType) {
        return storeTypes.getOrDefault(type, Set.of())
            .stream()
            .map(stores::get)
            .filter(expectedType::isInstance)
            .map(expectedType::cast)
            .toList();
    }

    public int size() {
        return stores.size();
    }
}
