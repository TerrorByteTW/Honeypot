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
import java.util.UUID;

public class DropBlockCountColumn06 implements Migration {
    private static class TempPlayers {
        public UUID playerName;
        public int blocksBroken;

        public TempPlayers(UUID uuid, int blocksBroken) {
            this.playerName = uuid;
            this.blocksBroken = blocksBroken;
        }
    }

    private final HoneypotLogger logger;

    public DropBlockCountColumn06(HoneypotLogger logger) {
        this.logger = logger;
    }

    @Override
    public void apply(SqliteDatabase.Tx tx) throws Exception {
        logger.debug(Component.text("Applying DropBlockCountColumn06 migration: Querying current block counts to populate new `honeypot_triggers` table"));
        List<TempPlayers> players = tx.query("""
                SELECT playerName, blocksBroken
                FROM honeypot_players
            """, row -> new TempPlayers(
            row.uuid("playerName"),
            row.i32("blocksBroken")
        ));

        logger.debug(Component.text("Applying DropBlockCountColumn06 migration: Dropping blocksBroken column from honeypot_players table"));
        tx.execute("ALTER TABLE honeypot_players DROP COLUMN blocksBroken;");

        logger.debug(Component.text("Applying DropBlockCountColumn06 migration: Adding new columns to honeypot_players table"));
        tx.execute("ALTER TABLE honeypot_players ADD COLUMN lifetimeActions INT NOT NULL DEFAULT 0;");
        tx.execute("ALTER TABLE honeypot_players ADD COLUMN lifetimeTriggers INT NOT NULL DEFAULT 0;");

        // Migrations run before HoneypotPlayerRepository#createSchema, so the table has to be created here. Keep these definitions in sync
        logger.debug(Component.text("Applying DropBlockCountColumn06 migration: Creating `honeypot_triggers` table"));
        tx.execute("""
            CREATE TABLE IF NOT EXISTS honeypot_triggers (
                `playerUUID` VARCHAR NOT NULL,
                `triggerType` VARCHAR NOT NULL,
                `count` INT NOT NULL,
                PRIMARY KEY (`playerUUID`, `triggerType`),
                FOREIGN KEY (`playerUUID`) REFERENCES honeypot_players (`playerName`)
            );
            """);

        logger.debug(Component.text("Applying DropBlockCountColumn06 migration: Carrying current block counts over to `honeypot_triggers` as BREAK triggers"));
        for (TempPlayers player : players) {
            tx.execute("""
                    INSERT INTO honeypot_triggers (playerUUID, triggerType, count)
                    VALUES (?, 'BREAK', ?)
                    ON CONFLICT (playerUUID, triggerType) DO UPDATE SET count = excluded.count;
                    """,
                Param.uuid(player.playerName),
                Param.i32(player.blocksBroken)
            );
        }

        // Every history row was a counted trigger, and "break" rows are the ones that also fired an action.
        // Admins can prune history, so these totals are a best-effort lower bound
        logger.debug(Component.text("Applying DropBlockCountColumn06 migration: Setting lifetimeTriggers and lifetimeActions based on the current history table"));
        tx.execute("""
            INSERT INTO honeypot_players (playerName, lifetimeTriggers, lifetimeActions)
            SELECT playerUUID, COUNT(*), SUM(type = 'break')
            FROM honeypot_history
            WHERE true
            GROUP BY playerUUID
            ON CONFLICT (playerName) DO UPDATE SET
                lifetimeTriggers = excluded.lifetimeTriggers,
                lifetimeActions = excluded.lifetimeActions;
            """);

        logger.debug(Component.text("DropBlockCountColumn06 migration applied successfully"));
    }
}
