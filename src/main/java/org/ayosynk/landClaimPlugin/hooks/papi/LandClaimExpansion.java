package org.ayosynk.landClaimPlugin.hooks.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LandClaimExpansion extends PlaceholderExpansion {

    private final LandClaimPlugin plugin;

    public LandClaimExpansion(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "landclaim";
    }

    @Override
    public @NotNull String getAuthor() {
        return "synk";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %landclaim_owner% - name of the owner of the claim at player's location
        if (params.equalsIgnoreCase("owner")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    return profile.getDisplayOwnerName();
                }
            }
            return "None";
        }

        // %landclaim_owner_uuid% - UUID of the owner of the claim at player's location
        if (params.equalsIgnoreCase("owner_uuid")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    return profile.getOwnerId().toString();
                }
            }
            return "None";
        }

        // %landclaim_name% - name of the claim at player's location
        if (params.equalsIgnoreCase("name")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    return profile.getName();
                }
            }
            return "None";
        }

        // %landclaim_is_claimed% - whether the chunk at player's location is claimed
        if (params.equalsIgnoreCase("is_claimed")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                return plugin.getClaimManager().isChunkClaimed(pos) ? "Yes" : "No";
            }
            return "No";
        }

        // %landclaim_members% - total members in the claim at player's location
        if (params.equalsIgnoreCase("members")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    return String.valueOf(profile.getMemberRoles().size());
                }
            }
            return "0";
        }

        // %landclaim_size% - total chunks in the claim at player's location
        if (params.equalsIgnoreCase("size")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    return String.valueOf(profile.getOwnedChunks().size());
                }
            }
            return "0";
        }

        // %landclaim_power% - current power level (placeholder for future mechanics)
        if (params.equalsIgnoreCase("power")) {
            return "0";
        }

        // %landclaim_pvp% - whether PvP is enabled in the claim at player's location
        if (params.equalsIgnoreCase("pvp")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    return profile.isPvpEnabled() ? "Enabled" : "Disabled";
                }
            }
            return "Disabled";
        }

        // %landclaim_role% - player's role in the claim at their location
        if (params.equalsIgnoreCase("role")) {
            if (player instanceof Player p) {
                ChunkPosition pos = new ChunkPosition(p.getLocation());
                ClaimProfile profile = plugin.getClaimManager().getProfileAt(pos);
                if (profile != null) {
                    if (profile.isOwner(p.getUniqueId())) return "Owner";
                    String role = profile.getMemberRole(p.getUniqueId());
                    return role != null ? role : "Visitor";
                }
            }
            return "Visitor";
        }

        // %landclaim_world% - world name at current location
        if (params.equalsIgnoreCase("world")) {
            if (player instanceof Player p) {
                return p.getWorld().getName();
            }
            return "None";
        }

        // %landclaim_x% - X coordinate
        if (params.equalsIgnoreCase("x")) {
            if (player instanceof Player p) {
                return String.valueOf(p.getLocation().getBlockX());
            }
            return "0";
        }

        // %landclaim_z% - Z coordinate
        if (params.equalsIgnoreCase("z")) {
            if (player instanceof Player p) {
                return String.valueOf(p.getLocation().getBlockZ());
            }
            return "0";
        }

        // %landclaim_profile% - name of the player's active profile
        if (params.equalsIgnoreCase("profile")) {
            if (player instanceof Player p) {
                ClaimProfile profile = plugin.getClaimManager().getActiveProfile(p);
                if (profile != null) {
                    return profile.getName();
                }
            }
            return "None";
        }

        // %landclaim_chunks% - total chunks in player's active profile
        if (params.equalsIgnoreCase("chunks") || params.equalsIgnoreCase("chunks_used")) {
            if (player instanceof Player p) {
                ClaimProfile profile = plugin.getClaimManager().getActiveProfile(p);
                if (profile != null) {
                    return String.valueOf(profile.getOwnedChunks().size());
                }
            }
            return "0";
        }

        // %landclaim_limit% - player's claim limit
        if (params.equalsIgnoreCase("limit") || params.equalsIgnoreCase("chunks_max")) {
            if (player instanceof Player p) {
                return String.valueOf(plugin.getClaimManager().getClaimLimit(p));
            }
            return "0";
        }

        // %landclaim_message:<key>% - returns a raw message from the config
        if (params.toLowerCase().startsWith("message:")) {
            String key = params.substring(8);
            return plugin.getConfigManager().getRawMessage(key);
        }

        return null;
    }
}
