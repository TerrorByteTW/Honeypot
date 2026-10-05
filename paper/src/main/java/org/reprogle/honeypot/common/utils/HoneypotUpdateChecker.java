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
import com.google.inject.Singleton;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.updater.ReleaseNotes;
import org.reprogle.honeypot.common.utils.updater.ServerVersionRange;
import org.reprogle.honeypot.common.utils.updater.VersionManifest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Downloads the version manifest to check for updates, and to verify that the server's Minecraft version is
 * supported by the running version of Honeypot.
 */
@Singleton
public class HoneypotUpdateChecker {

	public static final String MANIFEST_URL = "https://raw.githubusercontent.com/TerrorByteTW/Honeypot/refs/heads/master/version_manifest.yml";

	private static final int TIMEOUT_MILLIS = 10_000;

	private final JavaPlugin plugin;
	private final HoneypotLogger logger;
	private final CommandFeedback commandFeedback;

	@Inject
	HoneypotUpdateChecker(JavaPlugin plugin, HoneypotLogger logger, CommandFeedback commandFeedback) {
		this.plugin = plugin;
		this.logger = logger;
		this.commandFeedback = commandFeedback;
	}

	/**
	 * @return The version of Honeypot running on this server
	 */
	public String currentVersion() {
		return plugin.getPluginMeta().getVersion();
	}

	/**
	 * Asynchronously downloads and parses the version manifest. The consumer is called off the main thread, and is
	 * not called at all if the manifest can't be retrieved or parsed.
	 *
	 * @param consumer The consumer function, which accepts the parsed manifest
	 */
	public void fetchManifest(final Consumer<VersionManifest> consumer) {
		Bukkit.getAsyncScheduler().runNow(this.plugin, scheduledTask -> {
			logger.info(commandFeedback.sendCommandFeedback("updater.checking"));
			try {
				URLConnection connection = new URI(MANIFEST_URL).toURL().openConnection();
				connection.setConnectTimeout(TIMEOUT_MILLIS);
				connection.setReadTimeout(TIMEOUT_MILLIS);

				String yaml;
				try (InputStream inputStream = connection.getInputStream()) {
					yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
				}

				consumer.accept(ManifestParser.parse(yaml));
			} catch (IOException | URISyntaxException exception) {
				logger.info(commandFeedback.sendCommandFeedback("updater.check-failed",
					Placeholder.unparsed("reason", String.valueOf(exception.getMessage()))));
			} catch (InvalidConfigurationException exception) {
				logger.warning(commandFeedback.sendCommandFeedback("updater.manifest-invalid",
					Placeholder.unparsed("reason", String.valueOf(exception.getMessage()))));
			}
		});
	}

	/**
	 * Downloads the manifest once, then verifies the server version is supported and tells the console whether an
	 * update is available. Meant to be called when the plugin is enabled.
	 */
	public void checkOnStartup() {
		fetchManifest(manifest -> {
			checkIfServerSupported(manifest);
			notifyConsole(manifest);
		});
	}

	/**
	 * Downloads the manifest and verifies the server's Minecraft version is supported by this version of Honeypot.
	 * Any problems are logged as warnings.
	 */
	public void checkIfServerSupported() {
		fetchManifest(this::checkIfServerSupported);
	}

	/**
	 * Verifies the server's Minecraft version is supported by this version of Honeypot, using an already downloaded
	 * manifest. Also warns if this version of Honeypot has been pulled. Any problems are logged as warnings.
	 *
	 * @param manifest The parsed version manifest
	 */
	public void checkIfServerSupported(VersionManifest manifest) {
		String pluginVersion = currentVersion();
		Optional<ReleaseNotes> release = manifest.release(pluginVersion);

		if (release.isEmpty()) {
			logger.warning(commandFeedback.sendCommandFeedback("updater.version-not-in-manifest",
				Placeholder.unparsed("version", pluginVersion)));
			return;
		}

		if (release.get().pulled()) {
			String reason = release.get().pulledReason();
			logger.warning(reason == null
				? commandFeedback.sendCommandFeedback("updater.version-pulled-no-reason", Placeholder.unparsed("version", pluginVersion))
				: commandFeedback.sendCommandFeedback("updater.version-pulled", Placeholder.unparsed("version", pluginVersion), Placeholder.unparsed("reason", reason)));
			return;
		}

		ServerVersionRange range = release.get().supportedServerVersions();
		if (range == null) return;

		String serverVersion = Bukkit.getMinecraftVersion();
		if (!range.contains(serverVersion)) {
			logger.warning(commandFeedback.sendCommandFeedback("updater.server-unsupported"));
			logger.warning(commandFeedback.sendCommandFeedback("updater.supported-range",
				Placeholder.unparsed("version", pluginVersion),
				Placeholder.unparsed("min", range.min()),
				Placeholder.unparsed("max", range.max()),
				Placeholder.unparsed("server_version", serverVersion)));
		}
	}

	private void notifyConsole(VersionManifest manifest) {
		String currentVersion = currentVersion();
		if (manifest.hasUpdate(currentVersion)) {
			List<ReleaseNotes> newer = manifest.releasesNewerThan(currentVersion);
			int features = newer.stream().mapToInt(release -> release.features().size()).sum();
			int bugFixes = newer.stream().mapToInt(release -> release.bugFixes().size()).sum();
			plugin.getServer().getConsoleSender().sendMessage(commandFeedback.sendCommandFeedback("updater.update-available-console",
				Placeholder.unparsed("latest", manifest.latestVersion()),
				Placeholder.unparsed("features", String.valueOf(features)),
				Placeholder.unparsed("bug_fixes", String.valueOf(bugFixes)),
				Placeholder.unparsed("url", manifest.downloadUrl())));
		} else {
			plugin.getServer().getConsoleSender().sendMessage(commandFeedback.sendCommandFeedback("updater.up-to-date"));
		}
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
