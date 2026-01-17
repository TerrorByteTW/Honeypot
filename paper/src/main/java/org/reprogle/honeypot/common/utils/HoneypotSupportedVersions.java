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

public record HoneypotSupportedVersions(Plugin plugin, String version) {

    /**
     * Gets the supported server versions for the current version of the plugin
     *
     * @param consumer The consumer function
     */
    public void getSupportedVersions(final Consumer<VersionStatus> consumer, HoneypotLogger logger) {
        Bukkit.getAsyncScheduler().runNow(this.plugin, scheduledTask -> {
            logger.info(Component.text("Checking if this server version is supported"));
            try (InputStream inputStream = new URI("https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/supported-versions/" + version).toURL().openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (!scanner.hasNextLine()) return;

                String firstLine = scanner.nextLine().trim();

                if (firstLine.equalsIgnoreCase("pulled")) {
                    String reason = scanner.hasNextLine() ? scanner.nextLine() : "This version of Honeypot has been pulled, it's recommended you update to the next available version immediately!";
                    consumer.accept(new VersionStatus(true, reason));
                } else {
                    consumer.accept(new VersionStatus(false, firstLine));
                }
            } catch (IOException | URISyntaxException exception) {
                logger.warning(Component.text("Unable to check supported versions: " + exception.getMessage()));
            }
        });
    }
}
