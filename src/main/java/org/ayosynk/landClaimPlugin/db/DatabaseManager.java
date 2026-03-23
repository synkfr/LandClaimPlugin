package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Central database coordinator.
 * <p>
 * Routes to the appropriate {@link Database} implementation (SQLite or MySQL)
 * based on the plugin configuration. Initializes all DAO instances and manages
 * the connection lifecycle.
 */
public class DatabaseManager {
    private final LandClaimPlugin plugin;
    private Database database;
    private PlayerDao playerDao;
    private WarpDao warpDao;
    private ProfileDao profileDao;

    public DatabaseManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String type = plugin.getConfigManager().getPluginConfig().database.type;
        if (type.equalsIgnoreCase("MYSQL") || type.equalsIgnoreCase("MARIADB")) {
            // Validate MySQL credentials
            var dbConfig = plugin.getConfigManager().getPluginConfig().database;
            if (dbConfig.username.isEmpty() || dbConfig.password.isEmpty()) {
                plugin.getLogger().warning("MySQL/MariaDB is configured but username or password is empty. "
                        + "This may cause authentication failures. Please update your config.yml.");
            }
            database = new MySQLDatabase(plugin);
        } else {
            database = new SQLiteDatabase(plugin);
        }

        try {
            database.connect();
            plugin.getLogger().info("Successfully connected to the " + type + " database.");
            database.createTables();

            this.playerDao = new SQLPlayerDao(plugin, this);
            this.warpDao = new SQLWarpDao(plugin, this);
            this.profileDao = new SQLProfileDao(plugin, this);

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

    // ClaimDao removed as deprecated (V1 legacy). Use ProfileDao for V2 system.

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
