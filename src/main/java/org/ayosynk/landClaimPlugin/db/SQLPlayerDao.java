package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLPlayerDao implements PlayerDao {

    private final LandClaimPlugin plugin;
    private final DatabaseManager dbManager;

    public SQLPlayerDao(LandClaimPlugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @Override
    public void createTables() {
        String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
        String sql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "auto_claim BOOLEAN NOT NULL DEFAULT 0," +
                "auto_unclaim BOOLEAN NOT NULL DEFAULT 0," +
                "visualization_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT'," +
                "bonus_blocks INT NOT NULL DEFAULT 0)";

        try (Connection conn = dbManager.getDatabase().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create players table.");
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> savePlayer(ClaimPlayer player) {
        return CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = plugin.getConfigManager().getPluginConfig().database.type.equalsIgnoreCase("SQLITE")
                    ? "INSERT OR REPLACE INTO " + tablePrefix
                            + "players (uuid, auto_claim, auto_unclaim, visualization_mode, bonus_blocks) VALUES (?, ?, ?, ?, ?)"
                    : "INSERT INTO " + tablePrefix
                            + "players (uuid, auto_claim, auto_unclaim, visualization_mode, bonus_blocks) VALUES (?, ?, ?, ?, ?) "
                            +
                            "ON DUPLICATE KEY UPDATE auto_claim=VALUES(auto_claim), auto_unclaim=VALUES(auto_unclaim), visualization_mode=VALUES(visualization_mode), bonus_blocks=VALUES(bonus_blocks)";

            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, player.getUniqueId().toString());
                stmt.setBoolean(2, player.isAutoClaim());
                stmt.setBoolean(3, player.isAutoUnclaim());
                stmt.setString(4, player.getVisualizationMode());
                stmt.setInt(5, player.getBonusClaimBlocks());

                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player " + player.getUniqueId());
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<ClaimPlayer> getPlayer(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "players WHERE uuid = ?";

            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ClaimPlayer player = new ClaimPlayer(playerId);
                        player.setAutoClaim(rs.getBoolean("auto_claim"));
                        player.setAutoUnclaim(rs.getBoolean("auto_unclaim"));
                        player.setVisualizationMode(rs.getString("visualization_mode"));
                        player.setBonusClaimBlocks(rs.getInt("bonus_blocks"));
                        return player;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player " + playerId);
                e.printStackTrace();
            }
            // Return default player if not found
            return new ClaimPlayer(playerId);
        });
    }
}
