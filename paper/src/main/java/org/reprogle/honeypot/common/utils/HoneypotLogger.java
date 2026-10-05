/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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
     * Lock shared by every logger instance (Guice creates one per injection point), so concurrent writes to the log file don't interleave
     */
    private static final Object FILE_LOCK = new Object();

    /**
     * Override of the minimum log level for this logger instance. Null if not overridden
     */
    private final LogLevel overrideMinLevel;

    /**
     * Override of the log-to-file setting for this logger instance. Null if not overridden
     */
    private final Boolean overrideLogToFile;

    /**
     * Initialize the Honeypot logger and create it if it doesn't exist
     */
    @Inject
    public HoneypotLogger(@Named("HoneypotLogFile") File logFile, JavaPlugin plugin, BytePluginConfig config) {
        this(logFile, plugin, config, null, null);

        try {
            if (logFile.createNewFile()) {
                plugin.getLogger().info("Logs file created: " + logFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create the honeypot.log file for logging!");
        }
    }

    private HoneypotLogger(File logFile, JavaPlugin plugin, BytePluginConfig config, LogLevel overrideMinLevel, Boolean overrideLogToFile) {
        this.logFile = logFile;
        this.plugin = plugin;
        this.config = config;
        this.overrideMinLevel = overrideMinLevel;
        this.overrideLogToFile = overrideLogToFile;
    }

    /**
     * Returns a logger that uses the given minimum log level instead of the configured one.
     * This logger is not modified, so this is safe to use across threads, e.g. {@code logger.level(LogLevel.INFO).info(...)}
     *
     * @param level The minimum level for the returned logger
     * @return A HoneypotLogger with the override applied
     */
    public HoneypotLogger level(LogLevel level) {
        return new HoneypotLogger(logFile, plugin, config, level, overrideLogToFile);
    }

    /**
     * Returns a logger that uses the given log-to-file setting instead of the configured one.
     * This logger is not modified, so this is safe to use across threads
     *
     * @param logToFile Whether the returned logger logs to the file or not
     * @return A HoneypotLogger with the override applied
     */
    public HoneypotLogger logToFile(boolean logToFile) {
        return new HoneypotLogger(logFile, plugin, config, overrideMinLevel, logToFile);
    }

    /**
     * Returns a logger that always logs to the log file. This logger is not modified
     *
     * @return A HoneypotLogger with the override applied
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
        LogLevel minLevel = overrideMinLevel != null ? overrideMinLevel : configuredMinLevel();
        boolean logToFile = overrideLogToFile != null ? overrideLogToFile : config.config().getBoolean("logging.log-to-file");

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

        String line = "[" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "] " + level.name() + ": " + text + "\n";
        synchronized (FILE_LOCK) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
                bw.append(line);
            } catch (IOException e) {
                plugin.getLogger().warning("An error occurred while attempting to log to the honeypot.log file! " + e.getMessage());
            }
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
