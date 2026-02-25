package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLWarpDao implements WarpDao {

    private final LandClaimPlugin plugin;
    private final DatabaseManager dbManager;

    public SQLWarpDao(LandClaimPlugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @Override
    public void createTables() {
        String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;

        String sql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "warps (" +
                "player_id VARCHAR(36) NOT NULL," +
                "name VARCHAR(64) NOT NULL," +
                "world VARCHAR(64) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "PRIMARY KEY (player_id, name))";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create warps table: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Map<UUID, Map<String, Location>>> loadAllWarps() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Map<String, Location>> allWarps = new HashMap<>();
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "warps";

            try (Connection conn = dbManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    String name = rs.getString("name");
                    String worldName = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");

                    Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

                    allWarps.computeIfAbsent(playerId, k -> new HashMap<>()).put(name, loc);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load warps from database: " + e.getMessage());
            }
            return allWarps;
        });
    }

    @Override
    public void saveWarp(UUID playerId, String name, Location location) {
        CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "REPLACE INTO " + tablePrefix + "warps (player_id, name, world, x, y, z, yaw, pitch) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            if (dbManager.isMySQL()) {
                sql = "INSERT INTO " + tablePrefix + "warps (player_id, name, world, x, y, z, yaw, pitch) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)";
            }

            try (Connection conn = dbManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerId.toString());
                stmt.setString(2, name);
                stmt.setString(3, location.getWorld().getName());
                stmt.setDouble(4, location.getX());
                stmt.setDouble(5, location.getY());
                stmt.setDouble(6, location.getZ());
                stmt.setFloat(7, location.getYaw());
                stmt.setFloat(8, location.getPitch());
                stmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save warp to database: " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteWarp(UUID playerId, String name) {
        CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "DELETE FROM " + tablePrefix + "warps WHERE player_id = ? AND name = ?";

            try (Connection conn = dbManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerId.toString());
                stmt.setString(2, name);
                stmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete warp from database: " + e.getMessage());
            }
        });
    }
}
