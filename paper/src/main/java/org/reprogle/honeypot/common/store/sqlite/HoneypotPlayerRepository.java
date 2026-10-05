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

package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.storageproviders.HoneypotStore;
import org.reprogle.honeypot.common.storageproviders.PlayerStore;
import org.reprogle.honeypot.common.storageproviders.StoreType;
import org.reprogle.honeypot.common.store.HoneypotPlayerManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the SQLite Honeypot Store for Players. You should NOT interact with this directly.
 * Instead, use {@link HoneypotPlayerManager}
 */
@Singleton
@HoneypotStore(name = "sqlite-players", type = StoreType.PLAYER)
public class HoneypotPlayerRepository implements PlayerStore {
    private final SqliteDatabase db;

    @Inject
    public HoneypotPlayerRepository(HoneypotLogger logger, SqliteDatabase db, HoneypotMigrations migrations) {
        this.db = db;
        // Migrations must run before the schema is created, otherwise new tables may conflict with migrations
        migrations.migrate();
        logger.info(Component.text("Initializing Player table..."));
        createSchema();
        logger.info(Component.text("Player table initialized!"));
    }

    private void createSchema() {
        // Honeypot Players Table
        db.execute("""
            CREATE TABLE IF NOT EXISTS honeypot_players (
                `playerName` VARCHAR NOT NULL,
                `lifetimeActions` INT NOT NULL DEFAULT 0,
                `lifetimeTriggers` INT NOT NULL DEFAULT 0,
                PRIMARY KEY (`playerName`)
            );
            """);

        db.execute("""
            CREATE TABLE IF NOT EXISTS honeypot_triggers (
                `playerUUID` VARCHAR NOT NULL,
                `triggerType` VARCHAR NOT NULL,
                `count` INT NOT NULL,
                PRIMARY KEY (`playerUUID`, `triggerType`),
                FOREIGN KEY (`playerUUID`) REFERENCES honeypot_players (`playerName`)
            );
            """);
    }

    public void addPlayer(Player player, TriggerType triggerType, int triggered) {
        db.execute("""
                INSERT INTO honeypot_players (playerName, lifetimeTriggers) VALUES (?, ?) ON CONFLICT(playerName) DO UPDATE SET lifetimeTriggers = lifetimeTriggers + excluded.lifetimeTriggers;
                """,
            Param.uuid(player.getUniqueId()),
            Param.i32(triggered));

        db.execute("""
                INSERT INTO honeypot_triggers (playerUUID, triggerType, count) VALUES (?, ?, ?) ON CONFLICT (playerUUID, triggerType) DO UPDATE SET count = count + excluded.count;
                """,
            Param.uuid(player.getUniqueId()),
            Param.text(triggerType.name()),
            Param.i32(triggered));
    }

    public void resetPlayerCount(Player player, TriggerType trigger) {
        db.execute("""
                INSERT INTO honeypot_players (playerName) VALUES (?) ON CONFLICT (playerName) DO NOTHING;
                """,
            Param.uuid(player.getUniqueId()));

        db.execute("""
                INSERT INTO honeypot_triggers (playerUUID, triggerType, count)
                VALUES (?, ?, 0)
                ON CONFLICT (playerUUID, triggerType) DO UPDATE SET count = 0;
                """,
            Param.uuid(player.getUniqueId()),
            Param.text(trigger.name()));
    }

    public void playerTriggeredAction(Player player) {
        db.execute("""
                UPDATE honeypot_players SET lifetimeActions = lifetimeActions + 1 WHERE playerName = ?;
                """,
            Param.uuid(player.getUniqueId()));
    }

    public int getCount(Player player, TriggerType triggerType) {
        Integer count = db.queryOne("""
                SELECT count
                FROM honeypot_triggers
                WHERE playerUUID = ? AND triggerType = ?;
                """,
            row -> row.i32("count"),
            Param.uuid(player.getUniqueId()),
            Param.text(triggerType.name()));

        return count == null ? 0 : count;
    }

    public int getCount(OfflinePlayer player, TriggerType triggerType) {
        Integer count = db.queryOne("""
                SELECT count
                FROM honeypot_triggers
                WHERE playerUUID = ? AND triggerType = ?;
                """,
            row -> row.i32("count"),
            Param.uuid(player.getUniqueId()),
            Param.text(triggerType.name()));

        return count == null ? 0 : count;
    }

    public int getLifetimeTriggers(OfflinePlayer player) {
        Integer count = db.queryOne("""
                SELECT lifetimeTriggers
                FROM honeypot_players
                WHERE playerName = ?;
                """,
            row -> row.i32("lifetimeTriggers"),
            Param.uuid(player.getUniqueId()));

        return count == null ? 0 : count;
    }

    public int getLifetimeActions(OfflinePlayer player) {
        Integer count = db.queryOne("""
                SELECT lifetimeActions
                FROM honeypot_players
                WHERE playerName = ?;
                """,
            row -> row.i32("lifetimeActions"),
            Param.uuid(player.getUniqueId()));

        return count == null ? 0 : count;
    }

    public HashMap<TriggerType, Integer> getTriggerCounts(Player player) {
        List<Map.Entry<String, Integer>> rows = db.query("""
                SELECT triggerType, count
                FROM honeypot_triggers
                WHERE playerUUID = ?;
                """,
            row -> Map.entry(row.string("triggerType"), row.i32("count")),
            Param.uuid(player.getUniqueId()));

        HashMap<TriggerType, Integer> counts = new HashMap<>();
        for (Map.Entry<String, Integer> row : rows) {
            // Skip trigger types that no longer exist rather than failing the whole lookup
            try {
                counts.put(TriggerType.valueOf(row.getKey()), row.getValue());
            } catch (IllegalArgumentException ignored) {
                // Unknown trigger type
            }
        }

        return counts;
    }

    public void deleteAllHoneypotPlayers() {
        db.execute("DELETE FROM honeypot_triggers;");
        db.execute("DELETE FROM honeypot_players;");
    }
}
