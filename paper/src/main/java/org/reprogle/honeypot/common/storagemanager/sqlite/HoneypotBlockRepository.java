package org.reprogle.honeypot.common.storagemanager.sqlite;

import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.api.RowMapper;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;

import java.util.List;

public class HoneypotBlockRepository {
    private static final RowMapper<HoneypotBlockObject> BLOCK_OBJECT_MAPPER =
            row -> new HoneypotBlockObject(
                    row.string("world"),
                    row.i32("x_min"),
                    row.i32("y_min"),
                    row.i32("z_min"),
                    row.string("action")
            );

    @Inject
    SqliteDatabase db;

    public void createSchema() {
        // Honeypot Blocks Index, used for querying blocks. Honeypot Blocks table will reference the ID for the world and action
        db.execute("""
                CREATE VIRTUAL TABLE IF NOT EXISTS honeypot_index USING rtree(id INTEGER PRIMARY KEY, x_min INTEGER, x_max INTEGER, y_min INTEGER, y_max INTEGER, z_min INTEGER, z_max INTEGER);
                """);

        // Honeypot Blocks Table
        db.execute("""
                CREATE TABLE IF NOT EXISTS honeypot_blocks (
                    `id`     INTEGER PRIMARY KEY,
                    `world`  TEXT NOT NULL,
                    `action` TEXT NOT NULL,
                    FOREIGN KEY (`id`) REFERENCES honeypot_index(`id`) ON DELETE CASCADE
                );
                """);
    }

    public void createHoneypotBlock(Block block, String action) {

        db.transaction(tx -> {
            // 1) Insert the “real” row and get its generated id
            tx.execute("""
                            INSERT INTO honeypot_blocks (world, action)
                            VALUES (?, ?)
                            """,
                    Param.text(block.getWorld().getName()),
                    Param.text(action)
            );

            Long id = tx.queryOne("SELECT last_insert_rowid() AS id;",
                    row -> row.i64("id")
            );

            // 2) Insert rtree row using that id
            int x = block.getX(), y = block.getY(), z = block.getZ();
            tx.execute("""
                            INSERT INTO honeypot_index (id, x_min, x_max, y_min, y_max, z_min, z_max)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    Param.i64(id),
                    Param.i32(x), Param.i32(x),
                    Param.i32(y), Param.i32(y),
                    Param.i32(z), Param.i32(z)
            );

            return null;
        });
    }

    public void removeHoneypotBlock(Block block) {
        db.execute("""
                        DELETE FROM honeypot_blocks
                        WHERE id IN (
                            SELECT
                                id
                            FROM
                                honeypot_index
                            WHERE   x_min <= ? AND x_max >= ?
                              AND   y_min <= ? AND y_max >= ?
                              AND   z_min <= ? AND z_max >= ?
                        );
                        """,
                Param.text(Integer.toString(block.getX())),
                Param.text(Integer.toString(block.getX())),
                Param.text(Integer.toString(block.getY())),
                Param.text(Integer.toString(block.getY())),
                Param.text(Integer.toString(block.getZ())),
                Param.text(Integer.toString(block.getZ())));
        db.execute("""
                        DELETE FROM honeypot_index
                        WHERE   x_min <= ? AND x_max >= ?
                          AND   y_min <= ? AND y_max >= ?
                          AND   z_min <= ? AND z_max >= ?;
                        """,
                Param.text(Integer.toString(block.getX())),
                Param.text(Integer.toString(block.getX())),
                Param.text(Integer.toString(block.getY())),
                Param.text(Integer.toString(block.getY())),
                Param.text(Integer.toString(block.getZ())),
                Param.text(Integer.toString(block.getZ())));
    }

    public boolean isHoneypotBlock(Block block) {
        return db.queryOne("""
                        SELECT EXISTS (
                            SELECT 1 FROM honeypot_index
                            JOIN honeypot_blocks ON honeypot_index.id = honeypot_blocks.id
                            WHERE x_min <= ? AND x_max >= ?
                              AND y_min <= ? AND y_max >= ?
                              AND z_min <= ? AND z_max >= ?
                              AND world = ?
                        ) AS hit;
                        """,
                row -> row.i32("hit") == 1,
                Param.text(Integer.toString(block.getX())),
                Param.text(Integer.toString(block.getX())),
                Param.text(Integer.toString(block.getY())),
                Param.text(Integer.toString(block.getY())),
                Param.text(Integer.toString(block.getZ())),
                Param.text(Integer.toString(block.getZ())),
                Param.text(block.getWorld().getName()));
    }

    public @Nullable HoneypotBlockObject getHoneypotBlock(Block block) {
        return db.queryOne("""
                        SELECT
                            honeypot_index.x_min,
                            honeypot_index.y_min,
                            honeypot_index.z_min,
                            honeypot_blocks.world,
                            honeypot_blocks.action
                        FROM honeypot_index
                        JOIN honeypot_blocks ON honeypot_index.id = honeypot_blocks.id
                        WHERE x_min <= ? AND x_max >= ?
                          AND y_min <= ? AND y_max >= ?
                          AND z_min <= ? AND z_max >= ?
                          AND world = ?;
                        """,
                BLOCK_OBJECT_MAPPER,
                Param.i32(block.getX()),
                Param.i32(block.getX()),
                Param.i32(block.getY()),
                Param.i32(block.getY()),
                Param.i32(block.getZ()),
                Param.i32(block.getZ()),
                Param.text(block.getWorld().getName()));
    }

    public @Nullable String getAction(Block block) {
        return db.queryOne("""
                        SELECT honeypot_blocks.action
                        FROM honeypot_index
                        JOIN honeypot_blocks ON honeypot_index.id = honeypot_blocks.id
                        WHERE x_min <= ? AND x_max >= ?
                          AND y_min <= ? AND y_max >= ?
                          AND z_min <= ? AND z_max >= ?
                          AND world = ? ;
                        """,
                row -> row.string("action"),
                Param.i32(block.getX()),
                Param.i32(block.getX()),
                Param.i32(block.getY()),
                Param.i32(block.getY()),
                Param.i32(block.getZ()),
                Param.i32(block.getZ()),
                Param.text(block.getWorld().getName()));
    }

    public void deleteAllHoneypotBlocks(@Nullable World world) {
        db.execute("DELETE FROM honeypot_blocks;");
        db.execute("DELETE FROM honeypot_index;");
    }

    public List<HoneypotBlockObject> getAllHoneypots(@Nullable World world) {
        return db.query("""
                        SELECT
                            honeypot_index.x_min,
                            honeypot_index.y_min,
                            honeypot_index.z_min,
                            honeypot_blocks.world,
                            honeypot_blocks.action
                        FROM honeypot_index
                        JOIN honeypot_blocks ON honeypot_index.id = honeypot_blocks.id;
                        """,
                BLOCK_OBJECT_MAPPER);
    }

    public List<HoneypotBlockObject> getNearbyHoneypots(Location location, int radius) {
        int xMin = Math.min(location.getBlockX() - radius, location.getBlockX() + radius);
        int xMax = Math.max(location.getBlockX() - radius, location.getBlockX() + radius);
        int yMin = Math.min(location.getBlockY() - radius, location.getBlockY() + radius);
        int yMax = Math.max(location.getBlockY() - radius, location.getBlockY() + radius);
        int zMin = Math.min(location.getBlockZ() - radius, location.getBlockZ() + radius);
        int zMax = Math.max(location.getBlockZ() - radius, location.getBlockZ() + radius);

        return db.query("""
                        SELECT
                            honeypot_index.x_min,
                            honeypot_index.y_min,
                            honeypot_index.z_min,
                            honeypot_blocks.world,
                            honeypot_blocks.action
                        FROM honeypot_index
                        JOIN honeypot_blocks ON honeypot_index.id = honeypot_blocks.id
                        WHERE x_min <= ? AND x_max >= ?
                          AND y_min <= ? AND y_max >= ?
                          AND z_min <= ? AND z_max >= ?
                          AND world = ?;
                        """,
                BLOCK_OBJECT_MAPPER,
                Param.i32(xMin),
                Param.i32(xMax),
                Param.i32(yMin),
                Param.i32(yMax),
                Param.i32(zMin),
                Param.i32(zMax),
                Param.text(location.getWorld().getName()));
    }
}
