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

    public void deleteAllHoneypotPlayers() {
        db.execute("DELETE FROM honeypot_players;");
        db.execute("DELETE FROM honeypot_triggers;");
    }
}
