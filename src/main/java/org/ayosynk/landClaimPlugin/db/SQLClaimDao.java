package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLClaimDao implements ClaimDao {

    private final LandClaimPlugin plugin;
    private final DatabaseManager dbManager;

    public SQLClaimDao(LandClaimPlugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @Override
    public void createTables() {
        String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;

        String claimsSql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "claims (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "owner_id VARCHAR(36) NOT NULL," +
                "parent_id VARCHAR(36)," +
                "name VARCHAR(64)," +
                "claimed_at BIGINT NOT NULL," +
                "expire_at BIGINT NOT NULL)";

        String claimChunksSql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "claim_chunks (" +
                "claim_id VARCHAR(36) NOT NULL," +
                "chunk_world VARCHAR(64) NOT NULL," +
                "chunk_x INT NOT NULL," +
                "chunk_z INT NOT NULL," +
                "PRIMARY KEY (claim_id, chunk_world, chunk_x, chunk_z))";

        String claimRolesSql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "claim_roles (" +
                "claim_id VARCHAR(36) NOT NULL," +
                "player_id VARCHAR(36) NOT NULL," +
                "role_name VARCHAR(64) NOT NULL," +
                "PRIMARY KEY (claim_id, player_id))";

        try (Connection conn = dbManager.getDatabase().getConnection();
                PreparedStatement stmt1 = conn.prepareStatement(claimsSql);
                PreparedStatement stmt2 = conn.prepareStatement(claimChunksSql);
                PreparedStatement stmt3 = conn.prepareStatement(claimRolesSql)) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt3.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create claims tables.");
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> saveClaim(Claim claim) {
        return CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            boolean isSqlite = plugin.getConfigManager().getPluginConfig().database.type.equalsIgnoreCase("SQLITE");

            String saveClaimSql = isSqlite ? "INSERT OR REPLACE INTO " + tablePrefix
                    + "claims (id, owner_id, parent_id, name, claimed_at, expire_at) VALUES (?, ?, ?, ?, ?, ?)"
                    : "INSERT INTO " + tablePrefix
                            + "claims (id, owner_id, parent_id, name, claimed_at, expire_at) VALUES (?, ?, ?, ?, ?, ?) "
                            +
                            "ON DUPLICATE KEY UPDATE owner_id=VALUES(owner_id), parent_id=VALUES(parent_id), name=VALUES(name), expire_at=VALUES(expire_at)";

            String tryClearRolesSql = "DELETE FROM " + tablePrefix + "claim_roles WHERE claim_id = ?";
            String tryClearChunksSql = "DELETE FROM " + tablePrefix + "claim_chunks WHERE claim_id = ?";

            String insertRoleSql = isSqlite
                    ? "INSERT OR REPLACE INTO " + tablePrefix
                            + "claim_roles (claim_id, player_id, role_name) VALUES (?, ?, ?)"
                    : "INSERT INTO " + tablePrefix + "claim_roles (claim_id, player_id, role_name) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE role_name=VALUES(role_name)";

            String insertChunkSql = isSqlite
                    ? "INSERT OR REPLACE INTO " + tablePrefix
                            + "claim_chunks (claim_id, chunk_world, chunk_x, chunk_z) VALUES (?, ?, ?, ?)"
                    : "INSERT INTO " + tablePrefix
                            + "claim_chunks (claim_id, chunk_world, chunk_x, chunk_z) VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE chunk_world=VALUES(chunk_world)";

            try (Connection conn = dbManager.getDatabase().getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement stmt = conn.prepareStatement(saveClaimSql)) {
                    stmt.setString(1, claim.getId().toString());
                    stmt.setString(2, claim.getOwnerId().toString());
                    stmt.setString(3, claim.getParentClaimId() != null ? claim.getParentClaimId().toString() : null);
                    stmt.setString(4, claim.getName());
                    stmt.setLong(5, claim.getClaimedAt());
                    stmt.setLong(6, claim.getExpireAt());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(tryClearRolesSql)) {
                    stmt.setString(1, claim.getId().toString());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(tryClearChunksSql)) {
                    stmt.setString(1, claim.getId().toString());
                    stmt.executeUpdate();
                }

                if (!claim.getPlayerRoles().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertRoleSql)) {
                        for (Map.Entry<UUID, String> entry : claim.getPlayerRoles().entrySet()) {
                            stmt.setString(1, claim.getId().toString());
                            stmt.setString(2, entry.getKey().toString());
                            stmt.setString(3, entry.getValue());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                if (!claim.getChunks().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertChunkSql)) {
                        for (ChunkPosition pos : claim.getChunks()) {
                            stmt.setString(1, claim.getId().toString());
                            stmt.setString(2, pos.world());
                            stmt.setInt(3, pos.x());
                            stmt.setInt(4, pos.z());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save claim " + claim.getId());
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteClaim(UUID claimId) {
        return CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String delRoles = "DELETE FROM " + tablePrefix + "claim_roles WHERE claim_id = ?";
            String delChunks = "DELETE FROM " + tablePrefix + "claim_chunks WHERE claim_id = ?";
            String delClaim = "DELETE FROM " + tablePrefix + "claims WHERE id = ?";

            try (Connection conn = dbManager.getDatabase().getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement stmt1 = conn.prepareStatement(delRoles)) {
                    stmt1.setString(1, claimId.toString());
                    stmt1.executeUpdate();
                }

                try (PreparedStatement stmt2 = conn.prepareStatement(delChunks)) {
                    stmt2.setString(1, claimId.toString());
                    stmt2.executeUpdate();
                }

                try (PreparedStatement stmt3 = conn.prepareStatement(delClaim)) {
                    stmt3.setString(1, claimId.toString());
                    stmt3.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete claim " + claimId);
                e.printStackTrace();
            }
        });
    }

    private Claim constructClaim(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID ownerId = UUID.fromString(rs.getString("owner_id"));

        Claim claim = new Claim(id, ownerId);
        String parentId = rs.getString("parent_id");
        if (parentId != null)
            claim.setParentClaimId(UUID.fromString(parentId));
        claim.setName(rs.getString("name"));
        claim.setClaimedAt(rs.getLong("claimed_at"));
        claim.setExpireAt(rs.getLong("expire_at"));

        return claim;
    }

    private void loadClaimRoles(Connection conn, Claim claim) throws SQLException {
        String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
        String sql = "SELECT player_id, role_name FROM " + tablePrefix + "claim_roles WHERE claim_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, claim.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claim.setPlayerRole(UUID.fromString(rs.getString("player_id")), rs.getString("role_name"));
                }
            }
        }
    }

    private void loadClaimChunks(Connection conn, Claim claim) throws SQLException {
        String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
        String sql = "SELECT chunk_world, chunk_x, chunk_z FROM " + tablePrefix + "claim_chunks WHERE claim_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, claim.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claim.addChunk(
                            new ChunkPosition(rs.getString("chunk_world"), rs.getInt("chunk_x"), rs.getInt("chunk_z")));
                }
            }
        }
    }

    @Override
    public CompletableFuture<Claim> getClaim(UUID claimId) {
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "claims WHERE id = ?";
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, claimId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Claim claim = constructClaim(rs);
                        loadClaimRoles(conn, claim);
                        loadClaimChunks(conn, claim);
                        return claim;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load claim " + claimId);
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Claim>> getClaimsByOwner(UUID ownerId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Claim> claims = new ArrayList<>();
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "claims WHERE owner_id = ?";

            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, ownerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Claim claim = constructClaim(rs);
                        loadClaimRoles(conn, claim);
                        loadClaimChunks(conn, claim);
                        claims.add(claim);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load claims for owner " + ownerId);
                e.printStackTrace();
            }
            return claims;
        });
    }

    @Override
    public CompletableFuture<List<Claim>> getAllClaims() {
        return CompletableFuture.supplyAsync(() -> {
            List<Claim> claims = new ArrayList<>();
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "claims";

            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Claim claim = constructClaim(rs);
                    loadClaimRoles(conn, claim);
                    loadClaimChunks(conn, claim);
                    claims.add(claim);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load all claims");
                e.printStackTrace();
            }
            return claims;
        });
    }
}
