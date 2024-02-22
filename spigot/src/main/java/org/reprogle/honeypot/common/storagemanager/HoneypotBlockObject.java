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

package org.reprogle.honeypot.common.storagemanager;

import com.google.common.base.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HoneypotBlockObject implements PersistentDataType<String, HoneypotBlockObject> {

	private final String coordinates;

	private final String world;

	private final String action;

	/**
	 * Create a HoneypotBlockObject
	 *
	 * @param block  The Block object of the Honeypot
	 * @param action The action of the Honeypot
	 */
	public HoneypotBlockObject(Block block, String action) {
		this.coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
		this.world = block.getWorld().getName();
		this.action = action;
	}

	/**
	 * Used for GUI, create a Honeypot based off of strings and not Block objects
	 *
	 * @param worldName   The world the block is in
	 * @param coordinates The coordinates of the block
	 * @param action      The action of the Honeypot
	 */
	public HoneypotBlockObject(String worldName, String coordinates, String action) {
		this.coordinates = coordinates;
		this.world = worldName;
		this.action = action;
	}

	/**
	 * Get the String formatted coordinates of the Honeypot
	 *
	 * @return Coordinates
	 */
	public String getCoordinates() {
		return coordinates;
	}

	/**
	 * Get the Location object of the Honeypot
	 *
	 * @return Location
	 */
	public Location getLocation() {
		Pattern pattern = Pattern.compile("-?\\d+");
		Matcher matcher = pattern.matcher(coordinates);
		ArrayList<String> coords = new ArrayList<>();

		while (matcher.find()) {
			String coord = matcher.group();
			coords.add(coord);
		}

		int x = Integer.parseInt(coords.get(0));
		int y = Integer.parseInt(coords.get(1));
		int z = Integer.parseInt(coords.get(2));

		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	/**
	 * Get the action of the Honeypot
	 *
	 * @return action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Get the world of the Honeypot
	 *
	 * @return world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * Get the Block object of the Honeypot
	 *
	 * @return Honeypot Block object
	 */
	public Block getBlock() {
		Pattern pattern = Pattern.compile("-?\\d+");
		Matcher matcher = pattern.matcher(coordinates);
		ArrayList<String> coords = new ArrayList<>();

		while (matcher.find()) {
			String coord = matcher.group();
			coords.add(coord);
		}

		int x = Integer.parseInt(coords.get(0));
		int y = Integer.parseInt(coords.get(1));
		int z = Integer.parseInt(coords.get(2));

		return Bukkit.getWorld(world).getBlockAt(x, y, z);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HoneypotBlockObject honeypot))
			return false;
		if (o == this)
			return true;

		// Don't really care about the action since action doesn't determine a Honeypot
		return honeypot.coordinates.equalsIgnoreCase(this.coordinates) && honeypot.world.equals(this.world);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(coordinates, world, action);
	}

	// These methods are what allow HoneypotBlockObjects to directly be stored in
	// PDC. They may or may not be used, but they are here as utilities

	@Override
	public @NotNull Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public @NotNull Class<HoneypotBlockObject> getComplexType() {
		return HoneypotBlockObject.class;
	}

	@Override
	public String toPrimitive(@NotNull HoneypotBlockObject complex,
			@NotNull PersistentDataAdapterContext context) {
		return world + ";;" + coordinates + ";;" + action;
	}

	@Override
	public @NotNull HoneypotBlockObject fromPrimitive(String primitive,
			@NotNull PersistentDataAdapterContext context) {
		String[] strings = primitive.split(";;");
		return new HoneypotBlockObject(strings[0], strings[1], strings[2]);
	}

	public String toString() {
		return world + " " + coordinates + " " + action;
	}

}
