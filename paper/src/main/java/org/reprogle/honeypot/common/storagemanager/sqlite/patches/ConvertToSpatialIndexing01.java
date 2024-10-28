/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright TerrorByte & Honeypot Contributors (c) 2022 - 2024
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
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.sql.*;
import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class ConvertToSpatialIndexing01 implements SQLitePatch {

    private final String ADD_HONEYPOT_INDEX_TABLE = "CREATE VIRTUAL TABLE IF NOT EXISTS honeypot_index USING "
            + "rtree( id INTEGER PRIMARY KEY AUTOINCREMENT, x_min INTEGER, x_max INTEGER, y_min INTEGER, y_max INTEGER, z_min INTEGER, z_max INTEGER);";

    private final String CREATE_NEW_HONEYPOT_BLOCKS_TABLE = "CREATE TABLE IF NOT EXISTS new_honeypot_data (id INTEGER PRIMARY KEY," +
            "world TEXT NOT NULL," +
            "action TEXT NOT NULL," +
            "FOREIGN KEY (id) REFERENCES honeypot_index(id) ON DELETE CASCADE);";

    private final String DROP_OLD_HONEYPOT_DATA = "DROP TABLE IF EXISTS honeypot_blocks;";
    private final String RENAME_NEW_HONEYPOT_DATA_TABLE = "ALTER TABLE new_honeypot_data RENAME TO honeypot_blocks;";


    @Override
    public void update(Connection connection, HoneypotLogger logger) throws SQLException {
        ArrayList<HoneypotBlockObject> blocks = new ArrayList<>();
        ResultSet rs = null;

        logger.debug(Component.text("A rather large SQLite DB patch is about to be attempted. This may take a few seconds and cause your server to slightly lag"));

        connection.setAutoCommit(false);
        connection.createStatement().execute("PRAGMA busy_timeout = 5000;");

        try (Statement s = connection.createStatement()) {
            rs = s.executeQuery("SELECT * FROM honeypot_blocks;");

            while (rs.next()) {
                blocks.add(new HoneypotBlockObject(rs.getString("worldName"), rs.getString("coordinates"),
                        rs.getString("action")));
            }

            s.executeUpdate(ADD_HONEYPOT_INDEX_TABLE);
            s.executeUpdate(CREATE_NEW_HONEYPOT_BLOCKS_TABLE);

            for (HoneypotBlockObject block : blocks) {
                String[] coords = block.getCoordinates().split(", ");

                s.executeUpdate("INSERT INTO honeypot_index (x_min, x_max, y_min, y_max, z_min, z_max) VALUES (" +
                        coords[0] + ", " + coords[0] + ", " +
                        coords[1] + ", " + coords[1] + ", " +
                        coords[2] + ", " + coords[2] + ");");
                s.executeUpdate("INSERT INTO new_honeypot_data (id, world, action) VALUES (last_insert_rowid(),'" + block.getWorld() + "','" + block.getAction() + "');");
            }

            s.executeUpdate(DROP_OLD_HONEYPOT_DATA);
            s.executeUpdate(RENAME_NEW_HONEYPOT_DATA_TABLE);

            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            logger.debug(Component.text("Migrating to spacial indexing failed: " + e.getMessage()));
            connection.rollback();
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                logger.severe(Component.text("Failed to close SQLite Connection: " + e));
            }
        }
    }

    @Override
    public int patchedIn() {
        return 2;
    }
}
