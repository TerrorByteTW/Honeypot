package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.storageproviders.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.storageproviders.HoneypotStore;
import org.reprogle.honeypot.common.storageproviders.PlayerHistoryStore;
import org.reprogle.honeypot.common.storageproviders.StoreType;
import org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.List;

/**
 * Defines the SQLite Honeypot Store for PlayerHistory. You should NOT interact with this directly.
 * Instead, use {@link HoneypotPlayerHistoryManager}
 */
@Singleton
@HoneypotStore(name = "sqlite-player-history", type = StoreType.PLAYER_HISTORY)
public class HoneypotPlayerHistoryRepository implements PlayerHistoryStore {
    private final SqliteDatabase db;

    @Inject
    public HoneypotPlayerHistoryRepository(HoneypotLogger logger, SqliteDatabase db) {
        this.db = db;
        logger.info(Component.text("Initializing Player History table..."));
        createSchema();
        logger.info(Component.text("Player History table initialized!"));
    }

    private void createSchema() {
        // Honeypot History Table
        db.execute("""
            CREATE TABLE IF NOT EXISTS honeypot_history (
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
    }

    public void addPlayerHistory(Player p, Block block, String action, String type) {
        db.execute("""
                INSERT INTO honeypot_history (datetime, playerName, playerUUID, x, y, z, world, type, action)
                VALUES (DATETIME('now', 'localtime'), ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            Param.text(p.getName()),
            Param.text(p.getUniqueId().toString()),
            Param.i32(block.getX()),
            Param.i32(block.getY()),
            Param.i32(block.getZ()),
            Param.text(block.getWorld().getName()),
            Param.text(type),
            Param.text(action));
    }

    public List<HoneypotPlayerHistoryObject> getPlayerHistory(Player p) {
        return db.query("""
                SELECT *
                FROM honeypot_history
                WHERE playerUUID = ?
                ORDER BY datetime DESC;
                """,
            row -> new HoneypotPlayerHistoryObject(
                row.string("datetime"),
                row.string("playerName"),
                row.string("playerUUID"),
                new Location(
                    Bukkit.getWorld(row.string("world")),
                    row.i32("x"),
                    row.i32("y"),
                    row.i32("z")
                ),
                row.string("type"),
                row.string("action")
            ),
            Param.text(p.getUniqueId().toString()));
    }

    public void deletePlayerHistory(Player p, int... n) {
        if (n.length > 0) {
            db.execute("""
                    DELETE FROM honeypot_history
                    WHERE rowid IN (
                        SELECT rowid FROM honeypot_history
                        WHERE playerUUID = ?
                        ORDER BY rowid DESC
                        LIMIT ?
                    );
                    """,
                Param.text(p.getUniqueId().toString()),
                Param.i32(n[0]));
        } else {
            db.execute("""
                    DELETE FROM honeypot_history
                    WHERE playerUUID = ?;
                    """,
                Param.text(p.getUniqueId().toString()));
        }
    }

    public void deleteAllHistory() {
        db.execute("DELETE FROM honeypot_history;");
    }
}
