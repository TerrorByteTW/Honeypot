package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
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
    public HoneypotPlayerRepository(HoneypotLogger logger, SqliteDatabase db) {
        this.db = db;
        logger.info(Component.text("Initializing Player table..."));
        createSchema();
        logger.info(Component.text("Player table initialized!"));
    }

    private void createSchema() {
        // Honeypot Players Table
        db.execute("""
            CREATE TABLE IF NOT EXISTS honeypot_players (
                `playerName` VARCHAR NOT NULL,
                `blocksBroken` INT NOT NULL,
                PRIMARY KEY (`playerName`)
            );
            """);
    }

    public void addPlayer(Player player, int blocksBroken) {
        db.execute("""
                INSERT INTO honeypot_players (playerName, blocksBroken) VALUES (?, ?);
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
        Integer count = db.queryOne("""
                SELECT *
                FROM honeypot_players
                WHERE playerName = ?;
                """,
            row -> row.i32("blocksBroken"),
            Param.text(player.getUniqueId().toString()));

        return count == null ? -1 : count;
    }

    public int getCount(OfflinePlayer player) {
        Integer count = db.queryOne("""
                SELECT *
                FROM honeypot_players
                WHERE playerName = ?;
                """,
            row -> row.i32("blocksBroken"),
            Param.text(player.getUniqueId().toString()));

        return count == null ? -1 : count;
    }

    public void deleteAllHoneypotPlayers() {
        db.execute("DELETE FROM honeypot_players;");
    }
}
