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

package org.reprogle.honeypot.common.utils;

import org.bukkit.plugin.Plugin;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.utils.folia.Scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public record HoneypotUpdateChecker(Plugin plugin, String link) {

	/**
	 * Grabs the version number from the link provided
	 *
	 * @param consumer The consumer function
	 */
	public void getVersion(final Consumer<String> consumer) {
		Scheduler.runTaskAsynchronously(this.plugin, () -> {
			Honeypot.getHoneypotLogger().info("Checking for updates");
			try (InputStream inputStream = new URL(this.link).openStream();
					Scanner scanner = new Scanner(inputStream)) {
				if (scanner.hasNext()) {
					consumer.accept(scanner.next());
				}
			} catch (IOException exception) {
				Honeypot.getHoneypotLogger().info("Unable to check for updates" + exception.getMessage());
			}
		});
	}
}
