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

import com.google.common.base.Objects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public abstract class BehaviorProvider {

	protected final String providerName;
	protected final BehaviorType behaviorType;

	protected final Material icon;

	public BehaviorProvider() {
		this.providerName = getClass().getAnnotation(Behavior.class).name();
		this.behaviorType = getClass().getAnnotation(Behavior.class).type();
		this.icon = getClass().getAnnotation(Behavior.class).icon();
	}

	/**
	 * This should return the name of the behavior provider
	 *
	 * @return The name of the behavior provider
	 */
	public String getProviderName() {
		return providerName;
	}

	/**
	 * Return the type of the behavior. Currently type has no use
	 *
	 * @return The name of the behavior provider
	 */
	public BehaviorType getBehaviorType() {
		return behaviorType;
	}

	/**
	 * Return the type of the behavior. Currently, type has no use
	 *
	 * @return The name of the behavior provider
	 */
	public Material getIcon() {
		return icon;
	}

	/**
	 * Override default equals function to provide comparison support to BehaviorProviders.
	 * Since BehaviorProviders must have unique names, this checks against name only
	 *
	 * @param o The object which we are checking equality against
	 * @return True if the behavior providers are equal to each other
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BehaviorProvider behavior)) return false;
		if (o == this) return true;

		// Don't really care about the type or icon since providerName must be unique
		return behavior.getProviderName().equals(this.providerName);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(providerName, behaviorType, icon);
	}

	/**
	 * A method to be executed when an action requires processing.
	 * This is ignored if the <code>BehaviorType</code> is not set to <code>BehaviorTypes.CUSTOM</code>
	 *
	 * @param p The {@link org.bukkit.entity.Player} who the behavior provider will process against
	 * @return Your behavior provider should return true if the processing is successful, otherwise return false.
	 */
	public abstract boolean process(Player p, @Nullable Block block);
}
