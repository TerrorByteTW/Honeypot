package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.bytelib.db.migrate.MigrationStep;
import org.reprogle.bytelib.db.migrate.UserVersionMigrator;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.store.sqlite.patches.ConvertToSpatialIndexing01;
import org.reprogle.honeypot.common.store.sqlite.patches.MigratePdcToSqlite03;
import org.reprogle.honeypot.common.store.sqlite.patches.RemoveFKConstraint02;
import org.reprogle.honeypot.common.store.sqlite.patches.UpdateHistoryTable00;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.storageproviders.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.storageproviders.HoneypotStore;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the SQLite Honeypot Store. You should NOT interact with this directly.
 * Instead, use the {@link org.reprogle.honeypot.common.store.HoneypotBlockManager},
 * {@link org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager},
 * or {@link org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager}
 */
@Singleton
@HoneypotStore(name = "sqlite")
public class HoneypotRepository extends StorageProvider {

    private final HoneypotBlockRepository blockRepo;
    private final HoneypotPlayerHistoryRepository playerHistoryRepo;
    private final HoneypotPlayerRepository playerRepo;

    @Inject
    public HoneypotRepository(JavaPlugin plugin,
                              BytePluginConfig config,
                              HoneypotLogger logger,
                              HoneypotBlockRepository blockRepo,
                              HoneypotPlayerHistoryRepository playerHistoryRepo,
                              HoneypotPlayerRepository playerRepo,
                              SqliteDatabase db) {
        this.blockRepo = blockRepo;
        this.playerHistoryRepo = playerHistoryRepo;
        this.playerRepo = playerRepo;

        logger.info(Component.text("Checking and applying any necessary migrations..."));

        UserVersionMigrator migrator = new UserVersionMigrator("honeypot_blocks", new ArrayList<>(List.of(
            new MigrationStep(1, new UpdateHistoryTable00(logger)),
            new MigrationStep(2, new ConvertToSpatialIndexing01(logger)),
            new MigrationStep(3, new RemoveFKConstraint02(logger)),
            new MigrationStep(4, new MigratePdcToSqlite03(plugin, config, logger))
        )));
        migrator.migrate(db);

        logger.info(Component.text("Initializing SQLite database..."));
        blockRepo.createSchema();
        playerHistoryRepo.createSchema();
        playerRepo.createSchema();

    }

    /*
        Block Repository
     */
    public void createHoneypotBlock(Block block, String action) {
        this.blockRepo.createHoneypotBlock(block, action);
    }

    public void removeHoneypotBlock(Block block) {
        this.blockRepo.removeHoneypotBlock(block);
    }

    public boolean isHoneypotBlock(Block block) {
        return this.blockRepo.isHoneypotBlock(block);
    }

    public HoneypotBlockObject getHoneypotBlock(Block block) {
        return this.blockRepo.getHoneypotBlock(block);
    }

    public String getAction(Block block) {
        return this.blockRepo.getAction(block);
    }

    public void deleteAllHoneypotBlocks(@Nullable World world) {
        this.blockRepo.deleteAllHoneypotBlocks(world);
    }

    public List<HoneypotBlockObject> getAllHoneypots(@Nullable World world) {
        return this.blockRepo.getAllHoneypots(world);
    }

    public List<HoneypotBlockObject> getNearbyHoneypots(Location location, int radius) {
        return this.blockRepo.getNearbyHoneypots(location, radius);
    }

    /*
       Player Repository
     */
    public void addPlayer(Player player, int blocksBroken) {
        this.playerRepo.addPlayer(player, blocksBroken);
    }

    public void setPlayerCount(Player playerName, int blocksBroken) {
        this.playerRepo.setPlayerCount(playerName, blocksBroken);
    }

    public int getCount(Player player) {
        return this.playerRepo.getCount(player);
    }

    public int getCount(OfflinePlayer player) {
        return this.playerRepo.getCount(player);
    }

    public void deleteAllHoneypotPlayers() {
        this.playerRepo.deleteAllHoneypotPlayers();
    }

    /*
        Player History Repository
     */
    public void addPlayerHistory(Player p, HoneypotBlockObject block, String type) {
        this.playerHistoryRepo.addPlayerHistory(p, block, type);
    }

    public List<HoneypotPlayerHistoryObject> getPlayerHistory(Player p) {
        return this.playerHistoryRepo.getPlayerHistory(p);
    }

    public void deletePlayerHistory(Player p, int... n) {
        this.playerHistoryRepo.deletePlayerHistory(p, n);
    }

    public void deleteAllHistory() {
        this.playerHistoryRepo.deleteAllHistory();
    }

}
