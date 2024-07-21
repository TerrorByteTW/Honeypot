/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.reprogle.honeypot.Honeypot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HoneypotLogger {

	private final HoneypotConfigManager configManager;
	private final File logFile;
	private final Honeypot plugin;

	/**
	 * Initialize the Honeypot logger and create it if it doesn't exist
	 */
	@Inject
	public HoneypotLogger(@Named("HoneypotLogFile") File logFile, Honeypot plugin, HoneypotConfigManager configManager) {
		this.logFile = logFile;
		this.plugin = plugin;
		this.configManager = configManager;

		try {
			if (logFile.createNewFile()) {
				plugin.getLogger().info("Logs file created: " + logFile.getName());
			}
		}
		catch (IOException e) {
			plugin.getLogger().severe("Could not create the honeypot.log file for logging!");
		}
	}

	/**
	 * Log debug messages to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void debug(Component message) {
		if (!configManager.getPluginConfig().getBoolean("enable-logging"))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] DEBUG: ").append(message.toString()).append("\n");
		}
		catch (IOException e) {
			plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

	/**
	 * Log a message to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void info(Component message) {
		plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
		if (!configManager.getPluginConfig().getBoolean("enable-logging"))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] INFO: ").append(message.toString()).append("\n");
		}
		catch (IOException e) {
			plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

	/**
	 * Log a warning message to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void warning(Component message) {
		plugin.getLogger().warning(PlainTextComponentSerializer.plainText().serialize(message));
		if (!configManager.getPluginConfig().getBoolean("enable-logging"))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] WARNING: ").append(message.toString()).append("\n");
		}
		catch (IOException e) {
			plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

	/**
	 * Log a severe message to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void severe(Component message) {
		plugin.getLogger().severe(PlainTextComponentSerializer.plainText().serialize(message));
		if (!configManager.getPluginConfig().getBoolean("enable-logging"))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] SEVERE: ").append(message.toString()).append("\n");
		}
		catch (IOException e) {
			plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

}
