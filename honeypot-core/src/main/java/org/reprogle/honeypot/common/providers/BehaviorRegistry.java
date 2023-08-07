/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
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

package org.reprogle.honeypot.common.providers;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.common.providers.exceptions.BehaviorConflictException;
import org.reprogle.honeypot.common.providers.exceptions.InvalidBehaviorDefinitionException;

import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class BehaviorRegistry {

	private boolean initialized = false;

	private final Object lock = new Object();

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	protected final ConcurrentMap<String, BehaviorProvider> behaviorProviders = Maps.newConcurrentMap();

	/**
	 * Register a behavior provider with Honeypot
	 *
	 * @param behavior The {@link BehaviorProvider} that should be registered
	 */
	public void register(@NotNull BehaviorProvider behavior) {

		synchronized (lock) {
			if (initialized) {
				throw new IllegalStateException("New behaviors cannot be registered at this time");
			}

			try {
				forceRegister(behavior);
			} catch (InvalidBehaviorDefinitionException | BehaviorConflictException e) {
				Logger.getLogger("minecraft").warning(e.getMessage());
				Logger.getLogger("minecraft").warning("An error occurred while registering a behavior. Please see details above!");
			}
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	private BehaviorProvider forceRegister(BehaviorProvider behavior) throws InvalidBehaviorDefinitionException, BehaviorConflictException {

		synchronized (lock) {
			if (!behavior.getClass().isAnnotationPresent(Behavior.class))
				throw new InvalidBehaviorDefinitionException("Behavior " + behavior.getClass().getName().toLowerCase() + " is improperly defined, and therefore cannot be registered. Please contact the author of the plugin attempting to register this provider");

			if (behaviorProviders.containsKey(behavior.getProviderName().toLowerCase())) {
				throw new BehaviorConflictException("Behavior " + behavior.getClass().getName().toLowerCase() + " is already registered under that name. Please rename the Behavior");
			}

			return behaviorProviders.put(behavior.getProviderName().toLowerCase(), behavior);

		}

	}

	/**
	 * Returns a behavior provider based on registered name
	 *
	 * @param name The name of the provider to pull
	 * @return {@link BehaviorProvider} The behavior provider you requested
	 */
	public BehaviorProvider getBehaviorProvider(@NotNull String name) {
		return behaviorProviders.get(name.toLowerCase());
	}

	/**
	 * Returns all behavior providers
	 *
	 * @return A concurrent map of all behavior providers in the form of String, BehaviorProvider
	 */
	public ConcurrentMap<String, BehaviorProvider> getBehaviorProviders() {
		return behaviorProviders;
	}

	/**
	 * Get the size of the registry
	 *
	 * @return An int representing how many Behavior Providers are registered
	 */
	public int size() {
		return behaviorProviders.size();
	}
}
