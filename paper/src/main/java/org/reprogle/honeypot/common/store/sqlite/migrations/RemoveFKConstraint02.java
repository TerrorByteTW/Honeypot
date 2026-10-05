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

package org.reprogle.honeypot.common.store.sqlite.migrations;

import net.kyori.adventure.text.Component;
import org.reprogle.bytelib.db.migrate.Migration;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class RemoveFKConstraint02 implements Migration {

    private final HoneypotLogger logger;

    public RemoveFKConstraint02(HoneypotLogger logger) {
        this.logger = logger;
    }


    @Override
    public void apply(SqliteDatabase.Tx tx) {
        logger.debug(Component.text("Applying RemoveFKConstraint02 migration to SQLite database"));
        tx.execute("PRAGMA foreign_keys = OFF;");

        tx.execute("""
                CREATE TABLE IF NOT EXISTS honeypot_blocks_new (
                id INTEGER PRIMARY KEY,
                world TEXT NOT NULL,
                action TEXT NOT NULL
                );
                """);

        tx.execute("""
                INSERT INTO honeypot_blocks_new (id, world, action)
                SELECT id, world, action
                FROM honeypot_blocks;
                """);

        tx.execute("""
                DROP TABLE honeypot_blocks;
                """);

        tx.execute("""
                ALTER TABLE honeypot_blocks_new
                RENAME TO honeypot_blocks;
                """);
        tx.execute("PRAGMA foreign_keys = ON;");

        logger.debug(Component.text("Completed RemoveFKConstraint02 migration."));
    }
}
