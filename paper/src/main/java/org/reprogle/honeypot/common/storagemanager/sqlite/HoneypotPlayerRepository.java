package org.reprogle.honeypot.common.storagemanager.sqlite;

import com.google.inject.Inject;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

public class HoneypotPlayerRepository {
    @Inject
    SqliteDatabase db;

    public void createSchema() {
        // Honeypot Players Table
        db.execute("""
                CREATE TABLE IF NOT EXISTS honeypot_players (
                    `playerName` VARCHAR NOT NULL,
                    `blocksBroken` INT NOT NULL,
                    PRIMARY KEY (`playerName`)
                );
                """);
    }

    public void createHoneypotPlayer(Player player, int blocksBroken) {
        db.execute("""
                        INSET INTO honeypot_players (playerName, blocksBroken) VALUES (?, ?);
                        """,
                Param.text(player.getUniqueId().toString()),
                Param.i32(blocksBroken));
    }

    public void setPlayerCount(Player playerName, int blocksBroken) {
        db.execute("""
                        REPLACE INTO honeypot_players (playerName, blocksBroken) VALUES (?, ?);
                        """,
                Param.text(playerName.getUniqueId().toString()),
                Param.i32(blocksBroken));
    }

    public int getCount(Player player) {
        return db.queryOne("""
                        SELECT *
                        FROM honeypot_players
                        WHERE playerName = ?;
                        """,
                row -> row.i32("blocksBroken"),
                Param.text(player.getUniqueId().toString()));
    }

    public int getCount(OfflinePlayer player) {
        return db.queryOne("""
                        SELECT *
                        FROM honeypot_players
                        WHERE playerName = ?;
                        """,
                row -> row.i32("blocksBroken"),
                Param.text(player.getUniqueId().toString()));
    }

    public void deleteAllPlayers() {
        db.execute("DELETE FROM honeypot_players;");
    }
}
