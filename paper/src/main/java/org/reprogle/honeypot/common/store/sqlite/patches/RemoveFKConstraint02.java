package org.reprogle.honeypot.common.store.sqlite.patches;

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
        logger.debug(Component.text("Applying RemoveFKConstraint02 migration to SQLite database"), false);
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

        logger.debug(Component.text("Completed RemoveFKConstraint02 migration."), false);
    }
}
