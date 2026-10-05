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
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.migrate.Migration;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ConvertToSpatialIndexing01 implements Migration {

    private final String ADD_HONEYPOT_INDEX_TABLE = "CREATE VIRTUAL TABLE IF NOT EXISTS honeypot_index USING "
        + "rtree_i32( id INTEGER PRIMARY KEY, x_min INTEGER, x_max INTEGER, y_min INTEGER, y_max INTEGER, z_min INTEGER, z_max INTEGER);";

    // No FK to honeypot_index: virtual tables can't be FK parents (inserts fail with "foreign key mismatch" when FKs are enforced).
    // RemoveFKConstraint02 would strip it afterward anyway.
    private final String CREATE_NEW_HONEYPOT_BLOCKS_TABLE = "CREATE TABLE IF NOT EXISTS new_honeypot_data (id INTEGER PRIMARY KEY," +
        "world TEXT NOT NULL," +
        "action TEXT NOT NULL);";

    private final String DROP_OLD_HONEYPOT_DATA = "DROP TABLE IF EXISTS honeypot_blocks;";
    private final String RENAME_NEW_HONEYPOT_DATA_TABLE = "ALTER TABLE new_honeypot_data RENAME TO honeypot_blocks;";

    private final HoneypotLogger logger;

    public ConvertToSpatialIndexing01(HoneypotLogger logger) {
        this.logger = logger;
    }

    @Override
    public void apply(SqliteDatabase.Tx tx) {
        logger.info(Component.text("A rather large DB patch is going to be applied to the Honeypot DB. This may take a second..."));
        logger.debug(Component.text("Applying patch: ConvertToSpatialIndexing01"));
        // Read raw values rather than building Locations, since worlds may not be loaded while migrations run
        List<LegacyBlock> blocks = tx.query("""
                SELECT worldName, coordinates, action
                FROM honeypot_blocks
            """, row -> {
            String[] coords = row.string("coordinates").split(",");
            return new LegacyBlock(
                row.string("worldName"),
                Integer.parseInt(coords[0].trim()),
                Integer.parseInt(coords[1].trim()),
                Integer.parseInt(coords[2].trim()),
                row.string("action")
            );
        });

        tx.execute(ADD_HONEYPOT_INDEX_TABLE);
        tx.execute(CREATE_NEW_HONEYPOT_BLOCKS_TABLE);

        for (LegacyBlock block : blocks) {
            tx.execute("""
                        INSERT INTO honeypot_index (x_min, x_max, y_min, y_max, z_min, z_max)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """,
                Param.i32(block.x()), Param.i32(block.x()),
                Param.i32(block.y()), Param.i32(block.y()),
                Param.i32(block.z()), Param.i32(block.z()));

            Long id = tx.queryOne("SELECT last_insert_rowid() AS id;", row -> row.i64("id"));

            tx.execute("""
                        INSERT INTO new_honeypot_data (id, world, action)
                        VALUES (?, ?, ?)
                    """,
                Param.i64(id),
                Param.text(block.world()),
                Param.text(block.action()));
        }

        tx.execute(DROP_OLD_HONEYPOT_DATA);
        tx.execute(RENAME_NEW_HONEYPOT_DATA_TABLE);
        logger.debug(Component.text("Applied patch: ConvertToSpatialIndexing01"));
    }

    private record LegacyBlock(String world, int x, int y, int z, String action) {
    }
}
