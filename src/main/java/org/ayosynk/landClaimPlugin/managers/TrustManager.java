package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrustManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final ClaimManager claimManager;

    public TrustManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    public LandClaimPlugin getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    // Since trust is now handled on a per-claim basis via the Claim's playerRoles
    // map,
    // this manager's role is primarily to act as a bridge for checking those roles
    // against the configured Role permissions.

    public void initialize() {
        // Nothing to load on startup anymore since roles are in DB
        // and claims load their own roles when fetched.
    }

    public boolean addRoleToPlayer(Claim claim, UUID playerId, String roleName) {
        if (claim.getOwnerId().equals(playerId)) {
            return false;
        }

        // Validate role exists
        Role role = null;
        for (Role r : plugin.getCacheManager().getRoleCache().asMap().values()) {
            if (r.getName().equalsIgnoreCase(roleName)) {
                role = r;
                break;
            }
        }

        if (role == null)
            return false;

        claim.setPlayerRole(playerId, role.getName());

        // Save to DB async
        plugin.getDatabaseManager().getClaimDao().saveClaim(claim).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", claim.getId());
            }
        });

        return true;
    }

    public boolean removeRoleFromPlayer(Claim claim, UUID playerId) {
        if (!claim.getPlayerRoles().containsKey(playerId)) {
            return false;
        }

        claim.setPlayerRole(playerId, null);

        // Save to DB async
        plugin.getDatabaseManager().getClaimDao().saveClaim(claim).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", claim.getId());
            }
        });

        return true;
    }

    public String getPlayerRoleName(Claim claim, UUID playerId) {
        if (claim.getOwnerId().equals(playerId))
            return "Owner";
        return claim.getPlayerRole(playerId);
    }

    public boolean hasPermission(Claim claim, UUID playerId, String flag) {
        if (claim.getOwnerId().equals(playerId))
            return true;

        String roleName = claim.getPlayerRole(playerId);
        if (roleName == null)
            return false;

        for (Role r : plugin.getCacheManager().getRoleCache().asMap().values()) {
            if (r.getName().equalsIgnoreCase(roleName)) {
                return r.hasFlag(flag);
            }
        }

        return false;
    }

    public boolean canManageTrust(Claim claim, Player player) {
        if (claim.getOwnerId().equals(player.getUniqueId()))
            return true;
        return hasPermission(claim, player.getUniqueId(), "MANAGE_TRUST");
    }

    // Backwards compatibility methods during refactoring.
    // In v2, trust is purely claim-based, but we will leave these stubs for now to
    // prevent massive compilation errors.

    @Deprecated
    public boolean addTrustedPlayer(Player owner, String targetName) {
        return false;
    }

    @Deprecated
    public boolean addTrustedPlayer(Player owner, UUID trustedId) {
        return false;
    }

    @Deprecated
    public boolean removeTrustedPlayer(Player owner, String targetName) {
        return false;
    }

    @Deprecated
    public boolean removeTrustedPlayer(Player owner, UUID trustedId) {
        return false;
    }

    @Deprecated
    public boolean isTrusted(UUID ownerId, Player player) {
        return false;
    }

    @Deprecated
    public boolean hasTrustPermission(UUID ownerId, UUID trustedId, String permission) {
        return false;
    }

    @Deprecated
    public boolean hasVisitorPermission(UUID ownerId, String permission) {
        return false;
    }
}