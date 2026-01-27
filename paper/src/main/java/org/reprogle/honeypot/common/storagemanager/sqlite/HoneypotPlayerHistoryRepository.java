package org.reprogle.honeypot.common.storagemanager.sqlite;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.storageproviders.HoneypotPlayerHistoryObject;

import java.util.List;

public class HoneypotPlayerHistoryRepository {
    @Inject
    SqliteDatabase db;

    public void createSchema() {
        // Honeypot History Table
        db.execute("""
                CREATE TABLE IF NOT EXISTS honeypot_history (
                    `datetime` VARCHAR NOT NULL,
                    `playerName` VARCHAR NOT NULL,
                    `playerUUID` VARCHAR NOT NULL,
                    `coordinates` VARCHAR NOT NULL,
                    `world` VARCHAR NOT NULL,
                    `type` VARCHAR NOT NULL,
                    `action` VARCHAR NOT NULL
                );
                """);
    }

    public void addPlayerHistory(Player p, HoneypotBlockObject block, String type) {
        db.execute("""
                        INSERT INTO honeypot_history (datetime, playerName, playerUUID, coordinates, world, type, action)
                        VALUES (DATETIME('now', 'localtime'), ?, ?, ?, ?, ?, ?)
                        """,
                Param.text(p.getName()),
                Param.text(p.getUniqueId().toString()),
                Param.text(block.getCoordinates()),
                Param.text(block.getWorld()),
                Param.text(type),
                Param.text(block.getAction()));
    }

    public List<HoneypotPlayerHistoryObject> retrieveHistory(Player p) {
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
                        new HoneypotBlockObject(
                                row.string("world"),
                                row.string("coordinates"),
                                row.string("action")
                        ),
                        row.string("type")
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
