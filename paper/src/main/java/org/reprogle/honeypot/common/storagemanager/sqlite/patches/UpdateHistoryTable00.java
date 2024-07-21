/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storagemanager.sqlite.patches;

import net.kyori.adventure.text.Component;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.sql.SQLException;
import java.sql.Statement;

// This is titled 00 since, if we ever need any further patches to the database, we can create new files for them.
@SuppressWarnings("FieldCanBeLocal")
public class UpdateHistoryTable00 implements SQLitePatch {

    private final String UPDATE_PLAYER_TABLE_00 = "ALTER TABLE honeypot_history ADD `type` " +
            "VARCHAR NOT NULL default 'prelimBreak';";

    public void update(Statement statement, HoneypotLogger logger) throws SQLException {
        try {
            statement.executeUpdate(UPDATE_PLAYER_TABLE_00);
        } catch (SQLException e) {
            logger.debug(Component.text("Altering table honeypot_history to add `type` column failed. "
                    + "This could be concerning, please check the DB to see if this column was properly added. "
                    + "If not, contact the developer. Error: " + e.getMessage()));
        }
    }

    // This is the version of the patch that corresponds to the version of the DB that this patch was written for.
    // This will never change from 1, however other patches may have a different value
    // This could easily be a variable, but it's a method to ensure it gets added since we implement SQLitePatch
    public int patchedIn() {
        return 1;
    }
}
