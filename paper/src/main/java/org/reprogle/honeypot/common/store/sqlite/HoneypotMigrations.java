package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.reprogle.bytelib.db.migrate.MigrationStep;
import org.reprogle.bytelib.db.migrate.UserVersionMigrator;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.store.sqlite.migrations.*;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all Honeypot database migrations.
 */
public class HoneypotMigrations {
    private final HoneypotLogger logger;
    private final SqliteDatabase db;

    @Inject
    public HoneypotMigrations(HoneypotLogger logger,
                              SqliteDatabase db) {
        this.logger = logger;
        this.db = db;
    }

    public void migrate() {
        logger.info(Component.text("Checking and applying any necessary database migrations..."));

        UserVersionMigrator migrator = new UserVersionMigrator("honeypot_players", new ArrayList<>(List.of(
            new MigrationStep(1, new UpdateHistoryTable00(logger)),
            new MigrationStep(2, new ConvertToSpatialIndexing01(logger)),
            new MigrationStep(3, new RemoveFKConstraint02(logger)),
            // A fourth migration used to exist in Honeypot 4, but was simply for migrating users from 3.5.1 to 4 *if* they used PDC.
            // It no longer exists as it hard-coded some checks in place that have the potential to break servers moving forward if certain criteria were met.
            // UserVersionMigrator happily handles jumps in user versions without issue.
            new MigrationStep(5, new RegionizeTables04(logger))
        )));
        migrator.migrate(db);

        logger.info(Component.text("Database migrations completed"));
    }
}
