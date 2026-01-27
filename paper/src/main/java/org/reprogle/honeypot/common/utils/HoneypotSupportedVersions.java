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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

public class HoneypotSupportedVersions {

    @Inject
    JavaPlugin plugin;

    @Inject
    HoneypotLogger logger;

    /**
     * Check the GitHub repo of the plugin to verify the version of Spigot we're
     * running on is supported
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public void checkIfServerSupported() {
        String[] serverVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        int serverMajorVer = Integer.parseInt(serverVersion[0]);
        int serverMinorVer = Integer.parseInt(serverVersion[1]);
        int serverRevisionVer = serverVersion.length > 2 ? Integer.parseInt(serverVersion[2]) : 0;

        String pluginVersion = plugin.getPluginMeta().getVersion();
        // Check for any updates
        new SupportedVersions(plugin, pluginVersion).getSupportedVersions(value -> {
            if (value.pulled()) {
                logger.warning(Component.text(value.message()));
                return;
            }
            // Get the least supported and most supported server versions for this version
            // of Honeypot
            String[] lowerVersion = value.message().split("-")[0].split("\\.");
            String[] upperVersion = value.message().split("-")[1].split("\\.");

            int lowerMajorVer = Integer.parseInt(lowerVersion[0]);
            int lowerMinorVer = Integer.parseInt(lowerVersion[1]);
            int lowerRevisionVer = lowerVersion.length > 2 ? Integer.parseInt(lowerVersion[2]) : 0;

            int upperMajorVer = Integer.parseInt(upperVersion[0]);
            int upperMinorVer = Integer.parseInt(upperVersion[1]);
            int upperRevisionVer = lowerVersion.length > 2 ? Integer.parseInt(upperVersion[2]) : 0;

            // Check if the version the server is running is within the bounds of the
            // supported versions
            // This check is done because it allows the plugin to verify and
            // disable version check messages without updating the plugin code
            // This means if a minor MC version rolls out and doesn't affect functionality
            // to the plugin, we can update it on the GitHub side and server admins will not
            // see an error message
            if ((serverMajorVer < lowerMajorVer || serverMajorVer > upperMajorVer)
                    && (serverMinorVer < lowerMinorVer || serverMinorVer >= upperMinorVer)
                    && (serverRevisionVer < lowerRevisionVer || serverRevisionVer > upperRevisionVer)) {
                logger.warning(
                        Component.text("Honeypot is not guaranteed to support this version of Minecraft. We won't prevent you from using it, but functionality is not guaranteed. If you experience any issues please report them to the developer."));
                logger.warning(Component.text("Honeypot " + pluginVersion + " supports server versions " + value));
            }
        }, logger);

    }

    public record SupportedVersions(Plugin plugin, String version) {

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
}
