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

package org.reprogle.honeypot.common.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

public record HoneypotUpdateChecker(Plugin plugin, String link) {

	/**
	 * Grabs the version number from the link provided
	 *
	 * @param consumer The consumer function
	 */
	public void getVersion(final Consumer<String> consumer, HoneypotLogger logger) {
		Bukkit.getAsyncScheduler().runNow(this.plugin, scheduledTask -> {
			logger.info(Component.text("Checking for updates"));
			try (InputStream inputStream = new URI(this.link).toURL().openStream();
				 Scanner scanner = new Scanner(inputStream)) {
				if (scanner.hasNext()) {
					consumer.accept(scanner.next());
				}
			} catch (IOException | URISyntaxException exception) {
				logger.info(Component.text("Unable to check for updates" + exception.getMessage()));
			}
		});
	}

	/**
	 * Compares two dotted version strings segment by segment (e.g. 5.2.0 vs 5.1.10). Missing segments are treated
	 * as 0, and any non-numeric suffix on a segment (e.g. "0-SNAPSHOT") is ignored.
	 *
	 * @return a negative number if a &lt; b, 0 if equal, positive if a &gt; b
	 */
	public static int compareVersions(String a, String b) {
		String[] aParts = a.trim().split("\\.");
		String[] bParts = b.trim().split("\\.");
		int length = Math.max(aParts.length, bParts.length);
		for (int i = 0; i < length; i++) {
			int aPart = i < aParts.length ? leadingInt(aParts[i]) : 0;
			int bPart = i < bParts.length ? leadingInt(bParts[i]) : 0;
			if (aPart != bPart)
				return Integer.compare(aPart, bPart);
		}
		return 0;
	}

	private static int leadingInt(String segment) {
		int end = 0;
		while (end < segment.length() && Character.isDigit(segment.charAt(end)))
			end++;
		return end == 0 ? 0 : Integer.parseInt(segment.substring(0, end));
	}
}
