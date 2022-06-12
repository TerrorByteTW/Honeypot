package me.terrorbyte.honeypot.storagemanager.sqlite;

import me.terrorbyte.honeypot.Honeypot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends Database{

    /**
     * Create an SQLite object from the instance
     * @param instance
     */
    public SQLite(Honeypot instance){
        super(instance);
    }

    // The queries used to load the DB table. Only runs if the table doesn't exist.
    private final String SQLITE_CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_players (" +
            "`playerName` varchar(10) NOT NULL," +
            "`blocksBroken` int(10) NOT NULL," +
            "PRIMARY KEY (`playerName`)" +
        ");";

    private final String SQLITE_CREATE_BLOCKS_TABLE = "CREATE TABLE IF NOT EXISTS honeypot_blocks (" +
            "`coordinates` varchar(10) NOT NULL," +
            "`worldName` varchar(10) NOT NULL," +
            "`action` varchar(10) NOT NULL," +
            "PRIMARY KEY (`coordinates`, `worldName`)" +
            ");";

    /**
     * Get's teh DB connection, also verifies if JDBC is installed. If it isn't plugin is disabled as it can't function without it
     * @return Connection if the connection is valid, otherwise returns null
     */
    public Connection getSQLConnection(){
        File dataFolder = new File(Honeypot.getPlugin().getDataFolder(), "honeypot.db");
        if(!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e){
                Honeypot.getPlugin().getLogger().severe("Could not create honeypot.db file");
            }
        }

        try {
            if(connection != null && !connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;

        } catch (SQLException e) {
            Honeypot.getPlugin().getLogger().severe("SQLite exception on initialize: " + e);
        } catch (ClassNotFoundException e){
            Honeypot.getPlugin().getLogger().severe("SQLite JDBC Library not found. Please install this on your PC to use SQLite: " + e);
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
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLITE_CREATE_PLAYERS_TABLE);
            s.executeUpdate(SQLITE_CREATE_BLOCKS_TABLE);
            s.close();
        } catch (SQLException e){
            e.printStackTrace();
        }

        initialize();

    }

}
