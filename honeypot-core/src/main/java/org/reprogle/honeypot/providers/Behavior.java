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

package org.reprogle.honeypot.providers;

import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An interface to define a Honeypot Behavior
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Behavior {

	/**
	 * The type of the BehaviorProvider. This is currently a formality and does not have any bearing on the provider functionality.
	 * Please define it correctly however, as this may receive added functionality in the future
	 *
	 * @return The type of the {@link BehaviorProvider}
	 */
	BehaviorType type();

	/**
	 * The name of the provider. Each provider must have a unique name
	 *
	 * @return The name of the {@link BehaviorProvider}
	 */
	String name();

	/**
	 * The icon of the provider to display within the GUI. Any {@link org.bukkit.Material} will do
	 *
	 * @return The {@link org.bukkit.Material} to display
	 */
	Material icon();
}
