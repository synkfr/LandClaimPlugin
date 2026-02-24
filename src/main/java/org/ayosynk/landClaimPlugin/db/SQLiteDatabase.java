package org.ayosynk.landClaimPlugin.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDatabase implements Database {
    private final LandClaimPlugin plugin;
    private HikariDataSource dataSource;

    public SQLiteDatabase(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() throws SQLException {
        File dataFolder = new File(plugin.getDataFolder(), "PlayerData");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "database.db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setPoolName("LandClaim-SQLite");

        // SQLite properties
        config.setMaximumPoolSize(1); // SQLite handles single write lock
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void createTables() {
        // Logic handled through DAO classes
    }
}
