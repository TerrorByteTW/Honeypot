package org.reprogle.honeypot.common.store.sqlite.patches;

import net.kyori.adventure.text.Component;
import org.reprogle.bytelib.db.migrate.Migration;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class RegionizeTables04  implements Migration {
    private final HoneypotLogger logger;

    public RegionizeTables04(HoneypotLogger logger) {
        this.logger = logger;
    }

    @Override
    public void apply(SqliteDatabase.Tx tx) throws Exception {
        logger.debug(Component.text("Applying RegionizeTables04 migration: Renaming honeypot_blocks to honeypot_regions"), false);
        tx.execute("ALTER TABLE honeypot_blocks RENAME TO honeypot_regions;");

        logger.debug(Component.text("Applying RegionizeTables04 migration: Creating honeypot_history_temp"), false);
        tx.execute("""
            CREATE TABLE IF NOT EXISTS honeypot_history_temp (
                    `datetime` VARCHAR NOT NULL,
                    `playerName` VARCHAR NOT NULL,
                    `playerUUID` VARCHAR NOT NULL,
                    `x` INTEGER NOT NULL,
                    `y` INTEGER NOT NULL,
                    `z` INTEGER NOT NULL,
                    `world` VARCHAR NOT NULL,
                    `type` VARCHAR NOT NULL,
                    `action` VARCHAR NOT NULL
                );
            """);

        logger.debug(Component.text("Applying RegionizeTables04 migration: Migrating honeypot_history to honeypot_history_temp"), false);
        tx.execute("""
            INSERT INTO honeypot_history_temp (
                    `datetime`, `playerName`, `playerUUID`, `x`, `y`, `z`, `world`, `type`, `action`
            ) SELECT
                    `datetime`,
                    `playerName`,
                    `playerUUID`,
                    CAST(trim(substr(
                        coordinates,
                        1,
                        instr(coordinates, ',') - 1
                    )) AS INTEGER) AS x,
            
                    CAST(trim(substr(
                        substr(coordinates, instr(coordinates, ',') + 1),
                        1,
                        instr(substr(coordinates, instr(coordinates, ',') + 1), ',') - 1
                    )) AS INTEGER) AS y,
            
                    CAST(trim(substr(
                        substr(coordinates, instr(coordinates, ',') + 1),
                        instr(substr(coordinates, instr(coordinates, ',') + 1), ',') + 1
                    )) AS INTEGER) AS z,
                    `world`,
                    `type`,
                    `action`
            FROM honeypot_history;
            """);

        logger.debug(Component.text("Applying RegionizeTables04 migration: Dropping honeypot_history"), false);
        tx.execute("DROP TABLE honeypot_history;");

        logger.debug(Component.text("Applying RegionizeTables04 migration: Renaming honeypot_history_temp to honeypot_history"), false);
        tx.execute("ALTER TABLE honeypot_history_temp RENAME TO honeypot_history;");

        logger.debug(Component.text("RegionizeTables04 migration applied successfully"), false);
    }
}
