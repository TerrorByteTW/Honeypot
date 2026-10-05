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

package org.reprogle.honeypot.common.utils.updater;

import javax.annotation.Nullable;
import java.util.List;

/**
 * All features and bug fixes shipped in a single version of Honeypot, along with its compatibility information
 *
 * @param version                  The version these notes belong to, such as "5.1.0"
 * @param features                 The new features in this version
 * @param bugFixes                 The bug fixes in this version
 * @param supportedServerVersions  The Minecraft server versions this release supports, or null if not specified
 * @param pulled                   Whether this version has been pulled and should no longer be used
 * @param pulledReason             Why this version was pulled, or null if no reason was given
 */
public record ReleaseNotes(
    String version,
    List<ChangelogEntry> features,
    List<ChangelogEntry> bugFixes,
    @Nullable ServerVersionRange supportedServerVersions,
    boolean pulled,
    @Nullable String pulledReason
) {

    public ReleaseNotes {
        features = List.copyOf(features);
        bugFixes = List.copyOf(bugFixes);
    }
}
