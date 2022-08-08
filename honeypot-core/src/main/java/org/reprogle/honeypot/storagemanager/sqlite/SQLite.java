package org.reprogle.honeypot.storagemanager.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.reprogle.honeypot.Honeypot;

public class SQLite extends Database {

    /**
     * Create an SQLite object from the instance
     * 
     * @param instance
     */
    public SQLite(Honeypot instance) {
        super(instance);
    }

    // The queries used to load the DB table. Only runs if the table doesn't exist.
    private static final String SQLITE_CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_players (" +
            "`playerName` VARCHAR NOT NULL," +
            "`blocksBroken` INT NOT NULL," +
            "PRIMARY KEY (`playerName`)" +
        ");";

    private static final String SQLITE_CREATE_BLOCKS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_blocks (" +
            "`coordinates` VARCHAR NOT NULL," +
            "`worldName` VARCHAR NOT NULL," +
            "`action` VARCHAR NOT NULL," +
            "PRIMARY KEY (`coordinates`, `worldName`)" +
        ");";
            
    //SQLite has this cool feature where if no primary key is provided, the primary key defaults to the rowid. Nifty!
    private static final String SQLITE_CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_history (" +
            "`datetime` VARCHAR NOT NULL," +
            "`playerName` varchar NOT NULL," +
            "`playerUUID` VARCHAR NOT NULL," +
            "`coordinates` VARCHAR NOT NULL,"+
            "`world` VARCHAR NOT NULL,"+
            "`action` VARCHAR NOT NULL" +
        ");";
    /**
     * Get's the DB connection, also verifies if JDBC is installed. If it isn't plugin is disabled as it can't function
     * without it
     * 
     * @return Connection if the connection is valid, otherwise returns null
     */
    public Connection getSQLConnection() {
        File dataFolder = new File(Honeypot.getPlugin().getDataFolder(), "honeypot.db");
        if (!dataFolder.exists()) {
            try {
                boolean success = dataFolder.createNewFile();
                if (success) {
                    Honeypot.getPlugin().getLogger().info("Created data folder");
                } else {
                    Honeypot.getPlugin().getLogger().severe("Could not create data folder!");
                }
            }
            catch (IOException e) {
                Honeypot.getPlugin().getLogger().severe("Could not create honeypot.db file");
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;

        }
        catch (SQLException e) {
            Honeypot.getPlugin().getLogger().severe("SQLite exception on initialize: " + e);
        }
        catch (ClassNotFoundException e) {
            Honeypot.getPlugin().getLogger()
                    .severe("SQLite JDBC Library not found. Please install this on your PC to use SQLite: " + e);
            Honeypot.getPlugin().getPluginLoader().disablePlugin(Honeypot.getPlugin());
        }

        return null;
    }

    /**
     * Loads the DB
     */
    @Override
    public void load() {
        connection = getSQLConnection();
        try (Statement s = connection.createStatement()) {
            s.executeUpdate(SQLITE_CREATE_PLAYERS_TABLE);
            s.executeUpdate(SQLITE_CREATE_BLOCKS_TABLE);
            s.executeUpdate(SQLITE_CREATE_HISTORY_TABLE);
        }
        catch (SQLException e) {
            Honeypot.getPlugin().getLogger().severe("SQLException occured while attempting to create tables if they don't exist: " + e);
        }

        initialize();

    }

}
