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

package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.reprogle.bytelib.db.migrate.MigrationStep;
import org.reprogle.bytelib.db.migrate.UserVersionMigrator;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.store.sqlite.migrations.*;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all Honeypot database migrations. Repositories depend on this class so that migrations
 * are guaranteed to run before any repository creates its schema.
 */
@Singleton
public class HoneypotMigrations {
    private final HoneypotLogger logger;
    private final SqliteDatabase db;
    private boolean migrated = false;

    @Inject
    public HoneypotMigrations(HoneypotLogger logger,
                              SqliteDatabase db) {
        this.logger = logger;
        this.db = db;
    }

    public synchronized void migrate() {
        if (migrated) return;

        logger.info(Component.text("Checking and applying any necessary database migrations..."));

        UserVersionMigrator migrator = new UserVersionMigrator("honeypot_players", new ArrayList<>(List.of(
            new MigrationStep(1, new UpdateHistoryTable00(logger)),
            new MigrationStep(2, new ConvertToSpatialIndexing01(logger)),
            new MigrationStep(3, new RemoveFKConstraint02(logger)),
            // A fourth migration used to exist in Honeypot 4, but was simply for migrating users from 3.5.1 to 4 *if* they used PDC.
            // It no longer exists as it hard-coded some checks in place that have the potential to break servers moving forward if certain criteria were met.
            // UserVersionMigrator happily handles jumps in user versions without issue.
            new MigrationStep(5, new RegionizeTables04(logger)),
            new MigrationStep(6, new AddBlockToHistory05(logger)),
            new MigrationStep(7, new DropBlockCountColumn06(logger)),
            new MigrationStep(8, new ConvertIndexToInt32_07(logger))
        )));
        migrator.migrate(db);

        migrated = true;
        logger.info(Component.text("Database migrations completed"));
    }
}
