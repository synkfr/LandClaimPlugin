package org.ayosynk.landClaimPlugin.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.PluginConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase implements Database {
    private final LandClaimPlugin plugin;
    private HikariDataSource dataSource;

    public MySQLDatabase(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() throws SQLException {
        PluginConfig.DatabaseConfig dbConfig = plugin.getConfigManager().getPluginConfig().database;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbConfig.host + ":" + dbConfig.port + "/" + dbConfig.databaseName
                + "?useSSL=false&autoReconnect=true");
        config.setUsername(dbConfig.username);
        config.setPassword(dbConfig.password);
        config.setPoolName("LandClaim-MySQL");

        config.setMaximumPoolSize(dbConfig.maximumPoolSize);
        config.setMinimumIdle(dbConfig.minimumIdle);
        config.setConnectionTimeout(dbConfig.connectionTimeout);

        // Recommended HikariCP MySQL properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

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
