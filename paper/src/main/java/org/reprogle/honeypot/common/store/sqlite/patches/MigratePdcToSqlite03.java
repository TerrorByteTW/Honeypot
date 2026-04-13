package org.reprogle.honeypot.common.store.sqlite.patches;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.migrate.Migration;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MigratePdcToSqlite03 implements Migration {

    private final JavaPlugin plugin;
    private final HoneypotLogger logger;
    private final BytePluginConfig config;

    public MigratePdcToSqlite03(JavaPlugin plugin, BytePluginConfig config, HoneypotLogger logger) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void apply(SqliteDatabase.Tx tx) throws IOException {
        if (config.config().getString("storage-method").equalsIgnoreCase("pdc")) {
            logger.warning(Component.text("Honeypot has detected a legacy storage provider (PDC) is in use."));
            logger.warning(Component.text("As of Honeypot v4, the PDC Storage Provider is no longer supported. Honeypot will automatically migrate your PDC Honeypot blocks to SQLite. Beginning migration now..."));

            logger.debug(Component.text("Removing all existing Honeypot blocks from DB (if any) to prevent conflicts on write."), false);
            tx.execute("PRAGMA foreign_keys = OFF;");
            tx.execute("DELETE FROM honeypot_blocks;");
            tx.execute("DELETE FROM honeypot_index;");

            logger.debug(Component.text("Getting all Honeypot blocks from PDC and adding them to the SQLite DB..."), false);

            List<World> worlds = plugin.getServer().getWorlds();
            // To avoid potential issues when writing to the DB in a transaction, we're going to manually keep track of the row IDs
            long rowId = 1;

            for (World world : worlds) {
                List<HoneypotBlockObject> blocks = getAllHoneypots(world);
                for (HoneypotBlockObject block : blocks) {
                    tx.execute("""
                            INSERT INTO honeypot_blocks (id, world, action)
                            VALUES (?, ?, ?)
                            """,
                        Param.i64(rowId),
                        Param.text(block.getWorld()),
                        Param.text(block.getAction())
                    );

                    String[] coords = block.getCoordinates().split(", ");
                    int x = Integer.parseInt(coords[0]), y = Integer.parseInt(coords[1]), z = Integer.parseInt(coords[2]);

                    tx.execute("""
                            INSERT INTO honeypot_index (id, x_min, x_max, y_min, y_max, z_min, z_max)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                        Param.i64(rowId),
                        Param.i32(x), Param.i32(x),
                        Param.i32(y), Param.i32(y),
                        Param.i32(z), Param.i32(z)
                    );

                    // Increment the row ID to keep track of it.
                    rowId++;
                }
            }
            tx.execute("PRAGMA foreign_keys = OFF;");

            logger.debug(Component.text("All Honeypot blocks have been written to the DB, updating config..."), false);

            config.config().set("storage-method", "sqlite");
            config.config().save();
            config.reload();
            Registry.setStorageProvider(Registry.getStorageManagerRegistry().getStorageProvider("sqlite"));

            logger.info(Component.text("Migration to SQLite from PDC is complete and your config has been updated."));

        }
    }
    private List<HoneypotBlockObject> getAllHoneypots(World world) {
        List<HoneypotBlockObject> blocks = new ArrayList<>();
        Set<NamespacedKey> keys = world.getPersistentDataContainer().getKeys();

        for (NamespacedKey key : keys) {
            if (key.getKey().startsWith("honeypot-container-")) {
                String coordinatesRaw = key.getKey().split("honeypot-container-")[1];
                String coordinates = coordinatesRaw.replace("_", ", ");

                blocks.add(new HoneypotBlockObject(world.getName(), coordinates,
                    world.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
            }
        }

        return blocks;
    }
}
