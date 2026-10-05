/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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

/**
 * Converts {@code honeypot_index} from {@code rtree} (32-bit float coordinates, only exact up to ±16,777,216)
 * to {@code rtree_i32} (32-bit integer coordinates), so block coordinates near the world border are stored exactly.
 */
public class ConvertIndexToInt32_07 implements Migration {
    private final HoneypotLogger logger;

    public ConvertIndexToInt32_07(HoneypotLogger logger) {
        this.logger = logger;
    }

    @Override
    public void apply(SqliteDatabase.Tx tx) throws Exception {
        String indexSql = tx.queryOne("""
                SELECT sql
                FROM sqlite_master
                WHERE type = 'table' AND name = 'honeypot_index'
                LIMIT 1;
            """, row -> row.string("sql"));

        if (indexSql == null || indexSql.toLowerCase().contains("rtree_i32")) {
            logger.debug(Component.text("Skipping ConvertIndexToInt32_07 migration: honeypot_index is missing or already uses rtree_i32"));
            return;
        }

        // Migrations run before HoneypotRegionRepository#createSchema. Keep this definition in sync with it
        logger.debug(Component.text("Applying ConvertIndexToInt32_07 migration: Creating integer-based honeypot_index"));
        tx.execute("""
            CREATE VIRTUAL TABLE honeypot_index_new USING rtree_i32(id INTEGER PRIMARY KEY, x_min INTEGER, x_max INTEGER, y_min INTEGER, y_max INTEGER, z_min INTEGER, z_max INTEGER);
            """);

        logger.debug(Component.text("Applying ConvertIndexToInt32_07 migration: Copying existing index entries"));
        tx.execute("""
            INSERT INTO honeypot_index_new (id, x_min, x_max, y_min, y_max, z_min, z_max)
            SELECT id,
                   CAST(x_min AS INTEGER), CAST(x_max AS INTEGER),
                   CAST(y_min AS INTEGER), CAST(y_max AS INTEGER),
                   CAST(z_min AS INTEGER), CAST(z_max AS INTEGER)
            FROM honeypot_index;
            """);

        logger.debug(Component.text("Applying ConvertIndexToInt32_07 migration: Replacing old honeypot_index"));
        tx.execute("DROP TABLE honeypot_index;");
        tx.execute("ALTER TABLE honeypot_index_new RENAME TO honeypot_index;");

        logger.debug(Component.text("ConvertIndexToInt32_07 migration applied successfully"));
    }
}
