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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.reprogle.honeypot.common.utils.updater.ChangelogEntry;
import org.reprogle.honeypot.common.utils.updater.ReleaseNotes;
import org.reprogle.honeypot.common.utils.updater.ServerVersionRange;
import org.reprogle.honeypot.common.utils.updater.VersionManifest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Parses {@code version_manifest.yml} into a {@link VersionManifest}.
 * <p>
 * Versions are stored as a YAML list rather than as map keys, since Bukkit's YAML API treats dots in keys as path
 * separators (so a key of {@code 5.1.0} would otherwise be read as {@code 5 -> 1 -> 0}). The expected structure is:
 * <pre>{@code
 * manifest-version: 1
 * download-url: https://github.com/TerrorByteTW/Honeypot/releases
 * versions:
 *   - version: "5.1.0"
 *     supported-server-versions:
 *       min: "26.1"
 *       max: "26.3"
 *     pulled: false
 *     pulled-reason: "Only shown if pulled is true"
 *     features:
 *       - name: "Feature name"
 *         description: "A paragraph that will be wrapped automatically"
 *         material: NETHER_STAR
 *     bug-fixes:
 *       - name: "Bug fix name"
 *         description:
 *           - "Descriptions can also be a list,"
 *           - "in which case each line is shown as written"
 *         material: SPIDER_EYE
 * }</pre>
 */
public final class ManifestParser {

    public static final String DEFAULT_DOWNLOAD_URL = "https://github.com/TerrorByteTW/Honeypot/releases";

    private ManifestParser() {
    }

    /**
     * Parse the raw YAML contents of a version manifest
     *
     * @param yaml The raw YAML
     * @return The parsed manifest, with releases sorted newest first
     * @throws InvalidConfigurationException If the YAML is malformed or doesn't follow the manifest structure
     */
    public static VersionManifest parse(String yaml) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);

        int manifestVersion = config.getInt("manifest-version", 1);
        String downloadUrl = config.getString("download-url", DEFAULT_DOWNLOAD_URL);

        if (!config.isList("versions")) {
            throw new InvalidConfigurationException("Version manifest is missing the 'versions' list");
        }

        List<ReleaseNotes> releases = new ArrayList<>();
        for (Map<?, ?> rawRelease : config.getMapList("versions")) {
            releases.add(parseRelease(rawRelease));
        }

        releases.sort(Comparator.comparing(ReleaseNotes::version, HoneypotUpdateChecker::compareVersions).reversed());

        return new VersionManifest(manifestVersion, downloadUrl, releases);
    }

    private static ReleaseNotes parseRelease(Map<?, ?> rawRelease) throws InvalidConfigurationException {
        String version = asString(rawRelease.get("version"));
        if (version == null || version.isBlank()) {
            throw new InvalidConfigurationException("Every entry in 'versions' must have a 'version'");
        }

        return new ReleaseNotes(
            version.trim(),
            parseEntries(rawRelease.get("features"), version, "features"),
            parseEntries(rawRelease.get("bug-fixes"), version, "bug-fixes"),
            parseServerVersionRange(rawRelease.get("supported-server-versions"), version),
            Boolean.parseBoolean(String.valueOf(rawRelease.get("pulled"))),
            blankToNull(asString(rawRelease.get("pulled-reason")))
        );
    }

    @Nullable
    private static ServerVersionRange parseServerVersionRange(@Nullable Object rawRange, String version) throws InvalidConfigurationException {
        if (rawRange == null) return null;

        Map<?, ?> range = asMap(rawRange);
        String min = range == null ? null : blankToNull(asString(range.get("min")));
        String max = range == null ? null : blankToNull(asString(range.get("max")));
        if (min == null || max == null) {
            throw new InvalidConfigurationException("'supported-server-versions' in version " + version + " must have both a 'min' and a 'max'");
        }

        return new ServerVersionRange(min.trim(), max.trim());
    }

    private static List<ChangelogEntry> parseEntries(@Nullable Object rawEntries, String version, String section) throws InvalidConfigurationException {
        List<ChangelogEntry> entries = new ArrayList<>();
        if (rawEntries == null) return entries;

        if (!(rawEntries instanceof List<?> list)) {
            throw new InvalidConfigurationException("'" + section + "' in version " + version + " must be a list");
        }

        for (Object rawEntry : list) {
            Map<?, ?> entry = asMap(rawEntry);
            if (entry == null) {
                throw new InvalidConfigurationException("Every entry in '" + section + "' for version " + version + " must be a map");
            }

            String name = asString(entry.get("name"));
            if (name == null || name.isBlank()) {
                throw new InvalidConfigurationException("An entry in '" + section + "' for version " + version + " is missing a 'name'");
            }

            String material = blankToNull(asString(entry.get("material")));
            entries.add(new ChangelogEntry(name, parseDescription(entry.get("description")), material == null ? null : material.trim()));
        }

        return entries;
    }

    private static List<String> parseDescription(@Nullable Object rawDescription) {
        if (rawDescription == null) return List.of();

        if (rawDescription instanceof List<?> lines) {
            return lines.stream().map(String::valueOf).toList();
        }

        return List.of(String.valueOf(rawDescription));
    }

    @Nullable
    private static Map<?, ?> asMap(@Nullable Object value) {
        if (value instanceof Map<?, ?> map) return map;
        if (value instanceof ConfigurationSection section) return section.getValues(false);
        return null;
    }

    @Nullable
    private static String asString(@Nullable Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Nullable
    private static String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
