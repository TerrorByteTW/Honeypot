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

import org.reprogle.honeypot.Honeypot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HoneypotLogger {

	private File logFile;

	/**
	 * Initialize the Honeypot logger and create it if it doesn't exist
	 */
	public HoneypotLogger() {
		try {
			logFile = new File(Honeypot.plugin.getDataFolder(), "honeypot.log");
			if (logFile.createNewFile()) {
				Honeypot.plugin.getLogger().info("Logs file created: " + logFile.getName());
			}
		}
		catch (IOException e) {
			Honeypot.plugin.getLogger().severe("Could not create the honeypot.log file for logging!");
		}
	}

	/**
	 * Log debug messages to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void debug(String message) {
		if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-logging")))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] DEBUG: ").append(message).append("\n");
		}
		catch (IOException e) {
			Honeypot.plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

	/**
	 * Log a message to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void info(String message) {
		Honeypot.plugin.getLogger().info(message);
		if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-logging")))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] INFO: ").append(message).append("\n");
		}
		catch (IOException e) {
			Honeypot.plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

	/**
	 * Log a warning message to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void warning(String message) {
		Honeypot.plugin.getLogger().warning(message);
		if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-logging")))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] WARNING: ").append(message).append("\n");
		}
		catch (IOException e) {
			Honeypot.plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

	/**
	 * Log a severe message to the log file. Automatically prepends date and time
	 *
	 * @param message The message to log
	 */
	public void severe(String message) {
		Honeypot.plugin.getLogger().severe(message);
		if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-logging")))
			return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			bw.append("[").append(dtf.format(now)).append("] SEVERE: ").append(message).append("\n");
		}
		catch (IOException e) {
			Honeypot.plugin.getLogger()
					.warning("An error occured while attempting to log to the honeypot.log file! " + e);
		}
	}

}
