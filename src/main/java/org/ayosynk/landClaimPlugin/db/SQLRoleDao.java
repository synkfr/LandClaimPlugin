package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLRoleDao implements RoleDao {

    private final LandClaimPlugin plugin;
    private final DatabaseManager dbManager;

    public SQLRoleDao(LandClaimPlugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @Override
    public void createTables() {
        String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
        String sql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "roles (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "name VARCHAR(64) NOT NULL," +
                "priority INT NOT NULL," +
                "flags TEXT NOT NULL)";

        try (Connection conn = dbManager.getDatabase().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create roles table.");
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> saveRole(Role role) {
        return CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = plugin.getConfigManager().getPluginConfig().database.type.equalsIgnoreCase("SQLITE")
                    ? "INSERT OR REPLACE INTO " + tablePrefix + "roles (id, name, priority, flags) VALUES (?, ?, ?, ?)"
                    : "INSERT INTO " + tablePrefix + "roles (id, name, priority, flags) VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE name=VALUES(name), priority=VALUES(priority), flags=VALUES(flags)";

            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, role.getId().toString());
                stmt.setString(2, role.getName());
                stmt.setInt(3, role.getPriority());
                stmt.setString(4, String.join(",", role.getFlags()));

                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save role " + role.getName());
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteRole(UUID roleId) {
        return CompletableFuture.runAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "DELETE FROM " + tablePrefix + "roles WHERE id = ?";
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, roleId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete role " + roleId);
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Role> getRole(UUID roleId) {
        return CompletableFuture.supplyAsync(() -> {
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "roles WHERE id = ?";
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, roleId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Role role = new Role(roleId, null, rs.getString("name"), rs.getInt("priority"));
                        String flagsStr = rs.getString("flags");
                        if (flagsStr != null && !flagsStr.isEmpty()) {
                            for (String flag : flagsStr.split(",")) {
                                role.addFlag(flag);
                            }
                        }
                        return role;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load role " + roleId);
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Role>> getAllRoles() {
        return CompletableFuture.supplyAsync(() -> {
            List<Role> roles = new ArrayList<>();
            String tablePrefix = plugin.getConfigManager().getPluginConfig().database.tablePrefix;
            String sql = "SELECT * FROM " + tablePrefix + "roles";
            try (Connection conn = dbManager.getDatabase().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    UUID roleId = UUID.fromString(rs.getString("id"));
                    Role role = new Role(roleId, null, rs.getString("name"), rs.getInt("priority"));
                    String flagsStr = rs.getString("flags");
                    if (flagsStr != null && !flagsStr.isEmpty()) {
                        for (String flag : flagsStr.split(",")) {
                            role.addFlag(flag);
                        }
                    }
                    roles.add(role);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load all roles.");
                e.printStackTrace();
            }
            return roles;
        });
    }
}
