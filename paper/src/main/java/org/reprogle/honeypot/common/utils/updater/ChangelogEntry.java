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

import javax.annotation.Nullable;
import java.util.List;

/**
 * A single feature or bug fix listed in the version manifest
 *
 * @param name        The display name of the entry
 * @param description The description of the entry. A single-element list is treated as a paragraph and wrapped
 *                    automatically, while multi-element lists are displayed line-by-line as written
 * @param material    The raw material name to display this entry as, or null to use the section default
 */
public record ChangelogEntry(String name, List<String> description, @Nullable String material) {

    public ChangelogEntry {
        description = List.copyOf(description);
    }
}
