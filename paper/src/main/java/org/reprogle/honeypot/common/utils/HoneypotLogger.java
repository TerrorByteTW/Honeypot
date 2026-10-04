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

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final File logFile;
    private final JavaPlugin plugin;
    private final BytePluginConfig config;

    /**
     * A one-shot override of the minimum log level, applied to the next log call only. Null if not overridden
     */
    private LogLevel overrideMinLevel = null;

    /**
     * A one-shot override of the log-to-file setting, applied to the next log call only. Null if not overridden
     */
    private Boolean overrideLogToFile = null;

    /**
     * Initialize the Honeypot logger and create it if it doesn't exist
     */
    @Inject
    public HoneypotLogger(@Named("HoneypotLogFile") File logFile, JavaPlugin plugin, BytePluginConfig config) {
        this.logFile = logFile;
        this.plugin = plugin;
        this.config = config;

        try {
            if (logFile.createNewFile()) {
                plugin.getLogger().info("Logs file created: " + logFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create the honeypot.log file for logging!");
        }
    }

    /**
     * Method to temporarily override the log level. Only applies to the next log call
     *
     * @param level The level to set the logger to.
     * @return The HoneypotLogger instance for chaining
     */
    public HoneypotLogger level(LogLevel level) {
        this.overrideMinLevel = level;
        return this;
    }

    /**
     * Method to temporarily override the log file setting. Only applies to the next log call
     *
     * @param logToFile Whether to log to the file or not
     * @return The HoneypotLogger instance for chaining
     */
    public HoneypotLogger logToFile(boolean logToFile) {
        this.overrideLogToFile = logToFile;
        return this;
    }

    /**
     * Method to temporarily force logging to the log file. Only applies to the next log call
     *
     * @return The HoneypotLogger instance for chaining
     */
    public HoneypotLogger logToFile() {
        return logToFile(true);
    }

    /**
     * Log verbose messages to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void verbose(Component message) {
        log(LogLevel.VERBOSE, message);
    }

    /**
     * Log debug messages to the log file. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void debug(Component message) {
        log(LogLevel.DEBUG, message);
    }

    /**
     * Log a message to the console, and the log file if enabled. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void info(Component message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Log a warning message to the console, and the log file if enabled. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void warning(Component message) {
        log(LogLevel.WARNING, message);
    }

    /**
     * Log an error message to the console, and the log file if enabled. Automatically prepends date and time
     *
     * @param message The message to log
     */
    public void error(Component message) {
        log(LogLevel.ERROR, message);
    }

    private void log(LogLevel level, Component message) {
        // Settings are read on every call so that /honeypot reload takes effect, even for singletons holding a logger
        LogLevel minLevel;
        boolean logToFile;
        synchronized (this) {
            minLevel = overrideMinLevel != null ? overrideMinLevel : configuredMinLevel();
            logToFile = overrideLogToFile != null ? overrideLogToFile : config.config().getBoolean("logging.log-to-file");
            overrideMinLevel = null;
            overrideLogToFile = null;
        }

        if (!level.isAtLeast(minLevel))
            return;

        String text = PlainTextComponentSerializer.plainText().serialize(message);

        // VERBOSE and DEBUG are never output to console, and are always written to file
        switch (level) {
            case INFO -> plugin.getLogger().info(text);
            case WARNING -> plugin.getLogger().warning(text);
            case ERROR -> plugin.getLogger().severe(text);
            default -> logToFile = true;
        }

        if (minLevel == LogLevel.VERBOSE || minLevel == LogLevel.DEBUG)
            logToFile = true;

        if (!logToFile)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.append("[").append(TIMESTAMP_FORMAT.format(LocalDateTime.now())).append("] ")
                .append(level.name()).append(": ").append(text).append("\n");
        } catch (IOException e) {
            plugin.getLogger().warning("An error occurred while attempting to log to the honeypot.log file! " + e.getMessage());
        }
    }

    private LogLevel configuredMinLevel() {
        if (!config.config().getBoolean("logging.enable-logging"))
            return LogLevel.DISABLED;

        return LogLevel.fromString(config.config().getString("logging.minimum-log-level"), LogLevel.INFO);
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

        /**
         * Checks whether a message at this level should be logged when the logger's minimum level is {@code minLevel}
         *
         * @param minLevel The minimum level the logger is configured for
         * @return True if this level is at or above the minimum level, and neither level is DISABLED
         */
        public boolean isAtLeast(LogLevel minLevel) {
            if (this == DISABLED || minLevel == DISABLED) {
                return false;
            }
            return this.compareTo(minLevel) >= 0;
        }

        /**
         * Gets every level that will be logged when the logger's minimum level is {@code minLevel}
         *
         * @param minLevel The minimum level the logger is configured for
         * @return The set of active levels, empty if {@code minLevel} is DISABLED
         */
        public static Set<LogLevel> getActiveLevels(LogLevel minLevel) {
            if (minLevel == DISABLED) {
                return EnumSet.noneOf(LogLevel.class);
            }
            return EnumSet.range(minLevel, LogLevel.ERROR);
        }
    }
}
