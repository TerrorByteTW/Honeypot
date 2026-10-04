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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

public class HoneypotLogger {

    private final File logFile;
    private final JavaPlugin plugin;
    private LogLevel minLevel;
    private boolean logToFile;

    /**
     * A variable to store the log level temporarily when using the override methods
     */
    private LogLevel tempMinLevel = LogLevel.INFO;

    /**
     * A variable to store the log file setting temporarily when using the override methods
     */
    private boolean tempLogToFile = false;

    /**
     * A variable to store whether the logger has been overridden
     */
    private boolean overriden = false;

    /**
     * Initialize the Honeypot logger and create it if it doesn't exist
     */
    @Inject
    public HoneypotLogger(@Named("HoneypotLogFile") File logFile, JavaPlugin plugin, BytePluginConfig config) {
        this.logFile = logFile;
        this.plugin = plugin;

        if (!config.config().getBoolean("logging.enable-logging")) {
            this.minLevel = LogLevel.DISABLED;
        } else {
            this.minLevel = LogLevel.fromString(config.config().getString("logging.minimum-log-level"), LogLevel.INFO);
        }

        if (this.minLevel == LogLevel.VERBOSE || this.minLevel == LogLevel.DEBUG) {
            this.logToFile = true;
        } else {
            this.logToFile = config.config().getBoolean("logging.log-to-file");
        }

        try {
            if (logFile.createNewFile()) {
                plugin.getLogger().info("Logs file created: " + logFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create the honeypot.log file for logging!");
        }
    }

    /**
     * Log verbose messages to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void verbose(Component message) {
        if (!minLevel.isAtLeast(LogLevel.VERBOSE))
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            bw.append("[").append(dtf.format(now)).append("] VERBOSE: ").append(PlainTextComponentSerializer.plainText().serialize(message)).append("\n");
        } catch (IOException e) {
            plugin.getLogger().warning("An error occurred while attempting to log to the honeypot.log file for logging! " + e.getMessage());
        }
    }

    /**
     * Method to temporarily override the log level
     * @param level The level to set the logger to.
     * @return The HoneypotLogger instance for chaining
     */
    public HoneypotLogger level(LogLevel level) {
        this.tempMinLevel = this.minLevel;
        this.minLevel = (level != null) ? level : this.minLevel;
        return this;
    }

    /**
     * Method to temporarily override the log file setting
     * @param logToFile Whether to log to the file or not
     * @return The HoneypotLogger instance for chaining
     */
    public HoneypotLogger logToFile(boolean logToFile) {
        this.tempLogToFile = this.logToFile;
        this.logToFile = logToFile;
        return this;
    }

    /**
     * Method to temporarily override the log file setting
     * @return The HoneypotLogger instance for chaining
     */
    public HoneypotLogger logToFile() {
        this.tempLogToFile = this.logToFile;
        this.logToFile = true;
        return this;
    }

    /**
     * Resets the overridden values to their originals for the next call.
     * This is not always necessary since Guice will inject a Logger each time it's needed, but for
     * situations in which the same logger is reused, this method is required.
     */
    private void reset() {
        this.overriden = false;
        this.logToFile = this.tempLogToFile;
        this.minLevel = this.tempMinLevel;
        this.tempMinLevel = LogLevel.INFO;
        this.tempLogToFile = false;
    }

    /**
     * Log debug messages to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void debug(Component message) {
        if (!minLevel.isAtLeast(LogLevel.DEBUG))
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            bw.append("[").append(dtf.format(now)).append("] DEBUG: ").append(PlainTextComponentSerializer.plainText().serialize(message)).append("\n");
        } catch (IOException e) {
            plugin.getLogger()
                .warning("An error occurred while attempting to log to the honeypot.log file! " + e.getMessage());
        }

        if (overriden)
            this.reset();
    }

    /**
     * Log a message to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void info(Component message) {
        if (!minLevel.isAtLeast(LogLevel.INFO))
            return;

        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));

        if (!logToFile)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            bw.append("[").append(dtf.format(now)).append("] INFO: ").append(PlainTextComponentSerializer.plainText().serialize(message)).append("\n");
        } catch (IOException e) {
            plugin.getLogger()
                .warning("An error occurred while attempting to log to the honeypot.log file! " + e.getMessage());
        }

        if (overriden)
            this.reset();
    }

    /**
     * Log a warning message to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void warning(Component message) {
        if (!minLevel.isAtLeast(LogLevel.WARNING))
            return;

        plugin.getLogger().warning(PlainTextComponentSerializer.plainText().serialize(message));

        if (!logToFile)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            bw.append("[").append(dtf.format(now)).append("] WARNING: ").append(PlainTextComponentSerializer.plainText().serialize(message)).append("\n");
        } catch (IOException e) {
            plugin.getLogger()
                .warning("An error occurred while attempting to log to the honeypot.log file! " + e.getMessage());
        }

        if (overriden)
            this.reset();
    }

    /**
     * Log a severe message to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void error(Component message) {
        if (!minLevel.isAtLeast(LogLevel.ERROR))
            return;

        plugin.getLogger().severe(PlainTextComponentSerializer.plainText().serialize(message));

        if (!logToFile)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            bw.append("[").append(dtf.format(now)).append("] SEVERE: ").append(PlainTextComponentSerializer.plainText().serialize(message)).append("\n");
        } catch (IOException e) {
            plugin.getLogger()
                .warning("An error occurred while attempting to log to the honeypot.log file! " + e.getMessage());
        }

        if (overriden)
            this.reset();
    }

    public enum LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        DISABLED;

        public static LogLevel fromString(String input, LogLevel fallback) {
            if (input == null || input.isBlank()) {
                return fallback;
            }

            try {
                return LogLevel.valueOf(input.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return fallback;
            }

        }

        public boolean isAtLeast(LogLevel minLevel) {
            if (this == DISABLED) {
                return false;
            }
            return this.compareTo(minLevel) >= 0;
        }

        public static Set<LogLevel> getActiveLevels(LogLevel minLevel) {
            return EnumSet.range(minLevel, LogLevel.ERROR);
        }
    }
}
