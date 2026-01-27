package org.reprogle.honeypot.common.storagemanager.sqlite.patches;

import org.reprogle.bytelib.db.migrate.Migration;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

public class RemoveFKConstraint02 implements Migration {

    @Override
    public void apply(SqliteDatabase.Tx tx) {
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

    }
}
