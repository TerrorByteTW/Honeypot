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

import java.util.Arrays;

/**
 * The range of Minecraft server versions a release of Honeypot supports, inclusive on both ends.
 * <p>
 * The upper bound only compares as many segments as it specifies, so a max of {@code 26.3} includes every
 * {@code 26.3.x} hotfix, while a max of {@code 26.3.1} excludes {@code 26.3.2}.
 *
 * @param min The oldest supported server version, such as "26.1"
 * @param max The newest supported server version, such as "26.3"
 */
public record ServerVersionRange(String min, String max) {

    /**
     * @param serverVersion The Minecraft version of the server, such as "26.1.2"
     * @return True if the server version falls within this range
     */
    public boolean contains(String serverVersion) {
        if (HoneypotUpdateChecker.compareVersions(serverVersion, min) < 0) return false;

        int maxSegments = max.trim().split("\\.").length;
        String[] serverSegments = serverVersion.trim().split("\\.");
        String truncated = String.join(".", Arrays.copyOf(serverSegments, Math.min(maxSegments, serverSegments.length)));
        return HoneypotUpdateChecker.compareVersions(truncated, max) <= 0;
    }
}
