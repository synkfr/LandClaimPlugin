package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private final LandClaimPlugin plugin;
    private Database database;
    private ClaimDao claimDao;
    private PlayerDao playerDao;
    private WarpDao warpDao;
    private ProfileDao profileDao;

    public DatabaseManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String type = plugin.getConfigManager().getPluginConfig().database.type;
        if (type.equalsIgnoreCase("MYSQL") || type.equalsIgnoreCase("MARIADB")) {
            database = new MySQLDatabase(plugin);
        } else {
            database = new SQLiteDatabase(plugin);
        }

        try {
            database.connect();
            plugin.getLogger().info("Successfully connected to the " + type + " database.");
            database.createTables();

            this.claimDao = new SQLClaimDao(plugin, this);
            this.playerDao = new SQLPlayerDao(plugin, this);
            this.warpDao = new SQLWarpDao(plugin, this);
            this.profileDao = new SQLProfileDao(plugin, this);

            this.claimDao.createTables();
            this.playerDao.createTables();
            this.warpDao.createTables();
            this.profileDao.createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to the database! Disabling plugin.");
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void shutdown() {
        if (database != null) {
            database.disconnect();
            plugin.getLogger().info("Database connection closed.");
        }
    }

    public Database getDatabase() {
        return database;
    }

    public ClaimDao getClaimDao() {
        return claimDao;
    }

    public PlayerDao getPlayerDao() {
        return playerDao;
    }

    public WarpDao getWarpDao() {
        return warpDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    public Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    public boolean isMySQL() {
        String type = plugin.getConfigManager().getPluginConfig().database.type;
        return type.equalsIgnoreCase("MYSQL") || type.equalsIgnoreCase("MARIADB");
    }
}
