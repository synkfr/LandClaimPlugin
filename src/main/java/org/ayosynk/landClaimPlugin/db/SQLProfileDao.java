package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLProfileDao implements ProfileDao {

    private final LandClaimPlugin plugin;
    private final DatabaseManager dbManager;

    public SQLProfileDao(LandClaimPlugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    private String prefix() {
        return plugin.getConfigManager().getPluginConfig().database.tablePrefix;
    }

    private boolean isSqlite() {
        return plugin.getConfigManager().getPluginConfig().database.type.equalsIgnoreCase("SQLITE");
    }

    @Override
    public void createTables() {
        String p = prefix();

        String[] sqls = {
                "CREATE TABLE IF NOT EXISTS " + p + "claim_profiles ("
                        + "owner_id VARCHAR(36) PRIMARY KEY,"
                        + "name VARCHAR(64) NOT NULL)",

                "CREATE TABLE IF NOT EXISTS " + p + "claimed_chunks ("
                        + "chunk_id VARCHAR(128) PRIMARY KEY,"
                        + "owner_id VARCHAR(36) NOT NULL)",

                "CREATE TABLE IF NOT EXISTS " + p + "profile_roles ("
                        + "id VARCHAR(36) PRIMARY KEY,"
                        + "owner_id VARCHAR(36) NOT NULL,"
                        + "name VARCHAR(64) NOT NULL,"
                        + "priority INT NOT NULL,"
                        + "flags TEXT NOT NULL)",

                "CREATE TABLE IF NOT EXISTS " + p + "profile_trusted_players ("
                        + "owner_id VARCHAR(36) NOT NULL,"
                        + "player_id VARCHAR(36) NOT NULL,"
                        + "flags TEXT NOT NULL,"
                        + "PRIMARY KEY (owner_id, player_id))",

                "CREATE TABLE IF NOT EXISTS " + p + "profile_visitor_flags ("
                        + "owner_id VARCHAR(36) NOT NULL,"
                        + "flag VARCHAR(64) NOT NULL,"
                        + "PRIMARY KEY (owner_id, flag))",

                "CREATE TABLE IF NOT EXISTS " + p + "profile_member_roles ("
                        + "owner_id VARCHAR(36) NOT NULL,"
                        + "player_id VARCHAR(36) NOT NULL,"
                        + "role_name VARCHAR(64) NOT NULL,"
                        + "PRIMARY KEY (owner_id, player_id))",

                "CREATE TABLE IF NOT EXISTS " + p + "profile_ally_flags ("
                        + "owner_id VARCHAR(36) NOT NULL,"
                        + "ally_id VARCHAR(36) NOT NULL,"
                        + "flags TEXT NOT NULL,"
                        + "PRIMARY KEY (owner_id, ally_id))"
        };

        try (Connection conn = dbManager.getDatabase().getConnection()) {
            for (String sql : sqls) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create profile tables.");
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> saveProfile(ClaimProfile profile) {
        return CompletableFuture.runAsync(() -> {
            String p = prefix();
            boolean sqlite = isSqlite();

            String upsertProfile = sqlite
                    ? "INSERT OR REPLACE INTO " + p + "claim_profiles (owner_id, name) VALUES (?, ?)"
                    : "INSERT INTO " + p
                            + "claim_profiles (owner_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)";

            try (Connection conn = dbManager.getDatabase().getConnection()) {
                conn.setAutoCommit(false);

                // 1. Upsert profile
                try (PreparedStatement stmt = conn.prepareStatement(upsertProfile)) {
                    stmt.setString(1, profile.getOwnerId().toString());
                    stmt.setString(2, profile.getName());
                    stmt.executeUpdate();
                }

                String oid = profile.getOwnerId().toString();

                // 2. Clear and re-insert chunks
                clearTable(conn, p + "claimed_chunks", "owner_id", oid);
                if (!profile.getOwnedChunks().isEmpty()) {
                    String insertChunk = sqlite
                            ? "INSERT OR REPLACE INTO " + p + "claimed_chunks (chunk_id, owner_id) VALUES (?, ?)"
                            : "INSERT INTO " + p
                                    + "claimed_chunks (chunk_id, owner_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE owner_id=VALUES(owner_id)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertChunk)) {
                        for (ChunkPosition pos : profile.getOwnedChunks()) {
                            stmt.setString(1, pos.world() + ":" + pos.x() + ":" + pos.z());
                            stmt.setString(2, oid);
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 3. Clear and re-insert visitor flags
                clearTable(conn, p + "profile_visitor_flags", "owner_id", oid);
                if (!profile.getVisitorFlags().isEmpty()) {
                    String insertFlag = sqlite
                            ? "INSERT OR REPLACE INTO " + p + "profile_visitor_flags (owner_id, flag) VALUES (?, ?)"
                            : "INSERT INTO " + p
                                    + "profile_visitor_flags (owner_id, flag) VALUES (?, ?) ON DUPLICATE KEY UPDATE flag=VALUES(flag)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertFlag)) {
                        for (String flag : profile.getVisitorFlags()) {
                            stmt.setString(1, oid);
                            stmt.setString(2, flag);
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 4. Clear and re-insert trusted players
                clearTable(conn, p + "profile_trusted_players", "owner_id", oid);
                if (!profile.getTrustedPlayerFlags().isEmpty()) {
                    String insertTrusted = sqlite
                            ? "INSERT OR REPLACE INTO " + p
                                    + "profile_trusted_players (owner_id, player_id, flags) VALUES (?, ?, ?)"
                            : "INSERT INTO " + p
                                    + "profile_trusted_players (owner_id, player_id, flags) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE flags=VALUES(flags)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertTrusted)) {
                        for (Map.Entry<UUID, Set<String>> entry : profile.getTrustedPlayerFlags().entrySet()) {
                            stmt.setString(1, oid);
                            stmt.setString(2, entry.getKey().toString());
                            stmt.setString(3, String.join(",", entry.getValue()));
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 5. Clear and re-insert roles
                clearTable(conn, p + "profile_roles", "owner_id", oid);
                if (!profile.getRoles().isEmpty()) {
                    String insertRole = sqlite
                            ? "INSERT OR REPLACE INTO " + p
                                    + "profile_roles (id, owner_id, name, priority, flags) VALUES (?, ?, ?, ?, ?)"
                            : "INSERT INTO " + p
                                    + "profile_roles (id, owner_id, name, priority, flags) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), priority=VALUES(priority), flags=VALUES(flags)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertRole)) {
                        for (Role role : profile.getRoles().values()) {
                            stmt.setString(1, role.getId().toString());
                            stmt.setString(2, oid);
                            stmt.setString(3, role.getName());
                            stmt.setInt(4, role.getPriority());
                            stmt.setString(5, String.join(",", role.getFlags()));
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 6. Clear and re-insert member roles
                clearTable(conn, p + "profile_member_roles", "owner_id", oid);
                if (!profile.getMemberRoles().isEmpty()) {
                    String insertMember = sqlite
                            ? "INSERT OR REPLACE INTO " + p
                                    + "profile_member_roles (owner_id, player_id, role_name) VALUES (?, ?, ?)"
                            : "INSERT INTO " + p
                                    + "profile_member_roles (owner_id, player_id, role_name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE role_name=VALUES(role_name)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertMember)) {
                        for (Map.Entry<UUID, String> entry : profile.getMemberRoles().entrySet()) {
                            stmt.setString(1, oid);
                            stmt.setString(2, entry.getKey().toString());
                            stmt.setString(3, entry.getValue());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 7. Clear and re-insert allied claims
                clearTable(conn, p + "profile_ally_flags", "owner_id", oid);
                if (!profile.getAllyFlags().isEmpty()) {
                    String insertAlly = sqlite
                            ? "INSERT OR REPLACE INTO " + p
                                    + "profile_ally_flags (owner_id, ally_id, flags) VALUES (?, ?, ?)"
                            : "INSERT INTO " + p
                                    + "profile_ally_flags (owner_id, ally_id, flags) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE flags=VALUES(flags)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertAlly)) {
                        for (Map.Entry<UUID, Set<String>> entry : profile.getAllyFlags().entrySet()) {
                            stmt.setString(1, oid);
                            stmt.setString(2, entry.getKey().toString());
                            stmt.setString(3, String.join(",", entry.getValue()));
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save profile for " + profile.getOwnerId());
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteProfile(UUID ownerId) {
        return CompletableFuture.runAsync(() -> {
            String p = prefix();
            String oid = ownerId.toString();

            try (Connection conn = dbManager.getDatabase().getConnection()) {
                conn.setAutoCommit(false);

                clearTable(conn, p + "profile_ally_flags", "owner_id", oid);
                clearTable(conn, p + "profile_member_roles", "owner_id", oid);
                clearTable(conn, p + "profile_roles", "owner_id", oid);
                clearTable(conn, p + "profile_trusted_players", "owner_id", oid);
                clearTable(conn, p + "profile_visitor_flags", "owner_id", oid);
                clearTable(conn, p + "claimed_chunks", "owner_id", oid);

                try (PreparedStatement stmt = conn
                        .prepareStatement("DELETE FROM " + p + "claim_profiles WHERE owner_id = ?")) {
                    stmt.setString(1, oid);
                    stmt.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete profile for " + ownerId);
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<ClaimProfile> getProfile(UUID ownerId) {
        return CompletableFuture.supplyAsync(() -> {
            String p = prefix();
            try (Connection conn = dbManager.getDatabase().getConnection()) {
                // Load base profile
                try (PreparedStatement stmt = conn
                        .prepareStatement("SELECT * FROM " + p + "claim_profiles WHERE owner_id = ?")) {
                    stmt.setString(1, ownerId.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next())
                            return null;
                    }
                }

                String name;
                try (PreparedStatement stmt = conn
                        .prepareStatement("SELECT name FROM " + p + "claim_profiles WHERE owner_id = ?")) {
                    stmt.setString(1, ownerId.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next())
                            return null;
                        name = rs.getString("name");
                    }
                }

                ClaimProfile profile = new ClaimProfile(ownerId, name);
                loadChunks(conn, p, profile);
                loadVisitorFlags(conn, p, profile);
                loadTrustedPlayers(conn, p, profile);
                loadRoles(conn, p, profile);
                loadMemberRoles(conn, p, profile);
                loadAllyFlags(conn, p, profile);

                // Load warps from WarpManager to keep ClaimProfile in sync
                Map<String, org.ayosynk.landClaimPlugin.models.Warp> warps = plugin.getWarpManager().getWarps(ownerId);
                if (!warps.isEmpty()) {
                    for (org.ayosynk.landClaimPlugin.models.Warp warp : warps.values()) {
                        profile.addWarp(warp);
                    }
                }

                return profile;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load profile for " + ownerId);
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<ClaimProfile>> getAllProfiles() {
        return CompletableFuture.supplyAsync(() -> {
            List<ClaimProfile> profiles = new ArrayList<>();
            String p = prefix();
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + p + "claim_profiles");
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    UUID ownerId = UUID.fromString(rs.getString("owner_id"));
                    String name = rs.getString("name");
                    ClaimProfile profile = new ClaimProfile(ownerId, name);
                    loadChunks(conn, p, profile);
                    loadVisitorFlags(conn, p, profile);
                    loadTrustedPlayers(conn, p, profile);
                    loadRoles(conn, p, profile);
                    loadMemberRoles(conn, p, profile);
                    loadAllyFlags(conn, p, profile);

                    // Load warps from WarpManager to keep ClaimProfile in sync
                    Map<String, org.ayosynk.landClaimPlugin.models.Warp> warps = plugin.getWarpManager()
                            .getWarps(ownerId);
                    if (!warps.isEmpty()) {
                        for (org.ayosynk.landClaimPlugin.models.Warp warp : warps.values()) {
                            profile.addWarp(warp);
                        }
                    }

                    profiles.add(profile);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load all profiles.");
                e.printStackTrace();
            }
            return profiles;
        });
    }

    @Override
    public CompletableFuture<UUID> getProfileOwnerByMember(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String p = prefix();
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT owner_id FROM " + p + "profile_member_roles WHERE player_id = ?")) {
                stmt.setString(1, playerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return UUID.fromString(rs.getString("owner_id"));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to lookup member profile for " + playerId);
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<UUID> getProfileOwnerByTrusted(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String p = prefix();
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT owner_id FROM " + p + "profile_trusted_players WHERE player_id = ?")) {
                stmt.setString(1, playerId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return UUID.fromString(rs.getString("owner_id"));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to lookup trusted profile for " + playerId);
                e.printStackTrace();
            }
            return null;
        });
    }

    // --- Private loaders ---

    private void loadChunks(Connection conn, String p, ClaimProfile profile) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("SELECT chunk_id FROM " + p + "claimed_chunks WHERE owner_id = ?")) {
            stmt.setString(1, profile.getOwnerId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String[] parts = rs.getString("chunk_id").split(":");
                    if (parts.length == 3) {
                        profile.addChunk(new ChunkPosition(parts[0], Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2])));
                    }
                }
            }
        }
    }

    private void loadVisitorFlags(Connection conn, String p, ClaimProfile profile) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("SELECT flag FROM " + p + "profile_visitor_flags WHERE owner_id = ?")) {
            stmt.setString(1, profile.getOwnerId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profile.addVisitorFlag(rs.getString("flag"));
                }
            }
        }
    }

    private void loadTrustedPlayers(Connection conn, String p, ClaimProfile profile) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("SELECT player_id, flags FROM " + p + "profile_trusted_players WHERE owner_id = ?")) {
            stmt.setString(1, profile.getOwnerId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    String flagsStr = rs.getString("flags");
                    Set<String> flags = new HashSet<>();
                    if (flagsStr != null && !flagsStr.isEmpty()) {
                        for (String flag : flagsStr.split(",")) {
                            flags.add(flag.trim().toUpperCase());
                        }
                    }
                    profile.setTrustedFlags(playerId, flags);
                }
            }
        }
    }

    private void loadRoles(Connection conn, String p, ClaimProfile profile) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("SELECT * FROM " + p + "profile_roles WHERE owner_id = ?")) {
            stmt.setString(1, profile.getOwnerId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID roleId = UUID.fromString(rs.getString("id"));
                    Role role = new Role(roleId, profile.getOwnerId(), rs.getString("name"), rs.getInt("priority"));
                    String flagsStr = rs.getString("flags");
                    if (flagsStr != null && !flagsStr.isEmpty()) {
                        for (String flag : flagsStr.split(",")) {
                            role.addFlag(flag.trim());
                        }
                    }
                    profile.addRole(role);
                }
            }
        }
    }

    private void loadMemberRoles(Connection conn, String p, ClaimProfile profile) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement(
                        "SELECT player_id, role_name FROM " + p + "profile_member_roles WHERE owner_id = ?")) {
            stmt.setString(1, profile.getOwnerId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profile.setMemberRole(UUID.fromString(rs.getString("player_id")), rs.getString("role_name"));
                }
            }
        }
    }

    private void loadAllyFlags(Connection conn, String p, ClaimProfile profile) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("SELECT ally_id, flags FROM " + p + "profile_ally_flags WHERE owner_id = ?")) {
            stmt.setString(1, profile.getOwnerId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID allyId = UUID.fromString(rs.getString("ally_id"));
                    String flagsStr = rs.getString("flags");
                    Set<String> flags = new HashSet<>();
                    if (flagsStr != null && !flagsStr.isEmpty()) {
                        for (String flag : flagsStr.split(",")) {
                            flags.add(flag.trim().toUpperCase());
                        }
                    }
                    profile.setAllyFlags(allyId, flags);
                }
            }
        }
    }

    private void clearTable(Connection conn, String table, String column, String value) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table + " WHERE " + column + " = ?")) {
            stmt.setString(1, value);
            stmt.executeUpdate();
        }
    }
}
