/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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

import org.reprogle.honeypot.common.storageproviders.exceptions.InvalidStorageManagerDefinitionException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public interface Store {

    Map<Class<?>, HoneypotStore> CACHE =
        new ConcurrentHashMap<>();

    default HoneypotStore metadata() {
        return CACHE.computeIfAbsent(
            getClass(),
            clazz -> Optional.ofNullable(
                clazz.getAnnotation(HoneypotStore.class)
            ).orElseThrow(() ->
                new InvalidStorageManagerDefinitionException(getClass().getName() + " is improperly registered. The name cannot be retrieved at this time. Please reach out to the author of the plugin that attempted to register this Store."))
        );
    }

    default String getProviderName() {
        return metadata().name();
    }
}