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