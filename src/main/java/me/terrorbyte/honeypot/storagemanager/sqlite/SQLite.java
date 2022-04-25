package me.terrorbyte.honeypot.storagemanager.sqlite;

import me.terrorbyte.honeypot.Honeypot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends Database{

    public SQLite(Honeypot instance){
        super(instance);
    }

    public String SQLiteCreatePlayersTable = "CREATE TABLE IF NOT EXISTS honeypot_players (" +
            "`playerName` varchar(10) NOT NULL," +
            "`blocksBroken` int(10) NOT NULL," +
            "PRIMARY KEY (`playerName`)" +
        ");";

    public String SQLiteCreateBlocksTable = "CREATE TABLE IF NOT EXISTS honeypot_blocks (" +
            "`coordinates` varchar(10) NOT NULL," +
            "`worldName` varchar(10) NOT NULL," +
            "`action` varchar(10) NOT NULL," +
            "PRIMARY KEY (`coordinates`, `worldName`)" +
            ");";

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
            Honeypot.getPlugin().getLogger().severe("SQLite JBDC Library not found. Please install this on your PC to use SQLite: " + e);
        }

        return null;
    }

    @Override
    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreatePlayersTable);
            s.executeUpdate(SQLiteCreateBlocksTable);
            s.close();
        } catch (SQLException e){
            e.printStackTrace();
        }

        initialize();

    }

}
