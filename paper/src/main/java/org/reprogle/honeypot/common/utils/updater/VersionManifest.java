/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022-2026 TerrorByte and Honeypot Contributors.
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

package org.reprogle.honeypot.common.utils.updater;

import org.reprogle.honeypot.common.utils.HoneypotUpdateChecker;

import java.util.List;
import java.util.Optional;

/**
 * The parsed contents of {@code version_manifest.yml}
 *
 * @param manifestVersion The schema version of the manifest file
 * @param downloadUrl     Where players should be sent to download the latest version
 * @param releases        Every release in the manifest, sorted newest first
 */
public record VersionManifest(int manifestVersion, String downloadUrl, List<ReleaseNotes> releases) {

    public VersionManifest {
        releases = List.copyOf(releases);
    }

    /**
     * @return The newest version listed in the manifest that hasn't been pulled, or null if there is none
     */
    public String latestVersion() {
        return releases.stream()
            .filter(release -> !release.pulled())
            .map(ReleaseNotes::version)
            .findFirst()
            .orElse(null);
    }

    /**
     * @param version The version to look up, such as "5.1.0"
     * @return The release notes for that version, if it's listed in the manifest
     */
    public Optional<ReleaseNotes> release(String version) {
        return releases.stream()
            .filter(release -> HoneypotUpdateChecker.compareVersions(release.version(), version) == 0)
            .findFirst();
    }

    /**
     * @param currentVersion The version to compare against, typically the version running on the server
     * @return True if the manifest contains a version newer than {@code currentVersion}
     */
    public boolean hasUpdate(String currentVersion) {
        String latest = latestVersion();
        return latest != null && HoneypotUpdateChecker.compareVersions(latest, currentVersion) > 0;
    }

    /**
     * @param currentVersion The version to compare against, typically the version running on the server
     * @return Every release newer than {@code currentVersion}, sorted newest first
     */
    public List<ReleaseNotes> releasesNewerThan(String currentVersion) {
        return releases.stream()
            .filter(release -> HoneypotUpdateChecker.compareVersions(release.version(), currentVersion) > 0)
            .toList();
    }
}
