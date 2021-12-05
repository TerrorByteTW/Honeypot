package me.terrorbyte.honeypot.storagemanager.sqlite;

import me.terrorbyte.honeypot.Honeypot;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Database {

    Honeypot plugin;
    Connection connection;

    private final String playerTable = "honeypot_players";
    private final String blockTable = "honeypot_blocks";

    public Database(Honeypot instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + blockTable);
            ResultSet rs = ps.executeQuery();
            close(ps, rs);

        } catch (SQLException e) {
            Honeypot.getPlugin().getLogger().severe("Unable to retrieve connection: " + e);
        }
    }

    public void close(PreparedStatement ps, ResultSet rs){
        try{
            if(ps != null) ps.close();
            if(rs != null) rs.close();

        } catch (SQLException e) {
            Honeypot.getPlugin().getLogger().severe("Failed to close SQL connection: " + e);
        }
    }

    public void createHoneypotBlock(Block block, String action){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        String worldName = block.getWorld().getName();

        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("INSERT INTO " + blockTable + " (coordinates, action, worldName) VALUES (?, ?, ?);");
            ps.setString(1, coordinates);
            ps.setString(2, action);
            ps.setString(3, worldName);
            ps.executeUpdate();

        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing create SQL statement on block table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }
    }

    public void removeHoneypotBlock(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        String worldName = block.getWorld().getName();

        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("DELETE FROM " + blockTable + " WHERE coordinates = ? AND worldName = ?;");
            ps.setString(1, coordinates);
            ps.setString(2, worldName);
            ps.executeUpdate();

        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while remove executing SQL statement on block table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }
    }

    public boolean isHoneypotBlock(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        String worldName = block.getWorld().getName();

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("SELECT * FROM " + blockTable + " WHERE coordinates = ? AND worldName = ?;");
            ps.setString(1, coordinates);
            ps.setString(2, worldName);
            rs = ps.executeQuery();

            while(rs.next()){
                if(rs.getString("coordinates").equalsIgnoreCase(coordinates)){
                    return true;
                }
            }
        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing check SQL statement on block table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }

        return false;
    }

    public String getAction(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();
        String worldName = block.getWorld().getName();

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("SELECT * FROM " + blockTable + " WHERE coordinates = '" + coordinates + "' AND worldName = '" + worldName + "';");
            rs = ps.executeQuery();

            while(rs.next()){
                if(rs.getString("coordinates").equalsIgnoreCase(coordinates)){
                    return rs.getString("action");
                }
            }
        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing action SQL statement on block table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }

        return null;
    }

    public String getWorld(Block block){
        String coordinates = block.getX() + ", " + block.getY() + ", " + block.getZ();

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("SELECT * FROM " + blockTable + " WHERE coordinates = '" + coordinates + "';");
            rs = ps.executeQuery();

            while(rs.next()){
                if(rs.getString("coordinates").equalsIgnoreCase(coordinates)){
                    return rs.getString("worldName");
                }
            }
        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing world SQL statement on block table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }

        return null;
    }

    public void createHoneypotPlayer(String playerName, int blocksBroken){

        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("INSERT INTO " + playerTable + " (playerName, blocksBroken) VALUES (?, ?);");
            ps.setString(1, playerName);
            ps.setInt(2, blocksBroken);
            ps.executeUpdate();

        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing create SQL statement on player table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }
    }

    public void setPlayerCount(String playerName, int blocksBroken){

        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("REPLACE INTO " + playerTable + " (playerName, blocksBroken) VALUES (?, ?);");
            ps.setString(1, playerName);
            ps.setInt(2, blocksBroken);
            ps.executeUpdate();

        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing count update SQL statement on player table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }
    }

    public int getCount(String playerName){

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement("SELECT * FROM " + playerTable + " WHERE playerName = '" + playerName + "';");
            rs = ps.executeQuery();

            while(rs.next()){
                if(rs.getString("playerName").equalsIgnoreCase(playerName)){
                    return rs.getInt("blocksBroken");
                }
            }
        } catch (SQLException e){
            Honeypot.getPlugin().getLogger().severe("Error while executing count retrieval SQL statement on player table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e){
                Honeypot.getPlugin().getLogger().severe("Failed to close SQLite connection: " + e);
            }
        }

        return -1;
    }

}
