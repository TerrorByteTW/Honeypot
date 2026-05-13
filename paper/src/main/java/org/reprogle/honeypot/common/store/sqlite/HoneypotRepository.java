package org.reprogle.honeypot.common.store.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.bytelib.db.migrate.MigrationStep;
import org.reprogle.bytelib.db.migrate.UserVersionMigrator;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;
import org.reprogle.honeypot.common.store.sqlite.patches.*;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
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

    private final HoneypotRegionRepository regionRepo;
    private final HoneypotPlayerHistoryRepository playerHistoryRepo;
    private final HoneypotPlayerRepository playerRepo;

    @Inject
    public HoneypotRepository(JavaPlugin plugin,
                              BytePluginConfig config,
                              HoneypotLogger logger,
                              HoneypotRegionRepository regionRepo,
                              HoneypotPlayerHistoryRepository playerHistoryRepo,
                              HoneypotPlayerRepository playerRepo,
                              SqliteDatabase db) {
        this.regionRepo = regionRepo;
        this.playerHistoryRepo = playerHistoryRepo;
        this.playerRepo = playerRepo;

        logger.info(Component.text("Checking and applying any necessary migrations..."));

        UserVersionMigrator migrator = new UserVersionMigrator("honeypot_players", new ArrayList<>(List.of(
            new MigrationStep(1, new UpdateHistoryTable00(logger)),
            new MigrationStep(2, new ConvertToSpatialIndexing01(logger)),
            new MigrationStep(3, new RemoveFKConstraint02(logger)),
            new MigrationStep(4, new MigratePdcToSqlite03(plugin, config, logger)),
            new MigrationStep(5, new RegionizeTables04(logger))
        )));
        migrator.migrate(db);

        logger.info(Component.text("Initializing SQLite database..."));
        regionRepo.createSchema();
        playerHistoryRepo.createSchema();
        playerRepo.createSchema();

    }

    /*
        Block Repository
     */
    public void createHoneypotRegion(Block block, String action) {
        this.regionRepo.createHoneypotRegion(block, action);
    }

    public void createHoneypotRegion(Location pos1, Location pos2, String action) {
        this.regionRepo.createHoneypotRegion(pos1, pos2, action);
    }

    public void removeHoneypotRegion(Location location) {
        this.regionRepo.removeHoneypotRegion(location);
    }

    public boolean isHoneypot(Location location) {
        return this.regionRepo.isHoneypot(location);
    }

    public HoneypotRegionObject getHoneypotRegion(Location location) {
        return this.regionRepo.getHoneypotRegion(location);
    }

    public String getAction(Location location) {
        return this.regionRepo.getAction(location);
    }

    public void deleteAllHoneypotRegions() {
        this.regionRepo.deleteAllHoneypotRegions();
    }

    public List<HoneypotRegionObject> getAllHoneypotRegions() {
        return this.regionRepo.getAllHoneypotRegions();
    }

    public List<HoneypotRegionObject> getNearbyHoneypotRegions(Location location, int radius) {
        return this.regionRepo.getNearbyHoneypotRegions(location, radius);
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
    public void addPlayerHistory(Player p, Block block, String action, String type) {
        this.playerHistoryRepo.addPlayerHistory(p, block, action, type);
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
