package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ClaimManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<ChunkPosition, UUID> claimedChunks = new HashMap<>();
    private final Map<UUID, Set<ChunkPosition>> playerClaims = new HashMap<>();

    public ClaimManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        loadClaims();
    }

    public int getTotalClaims() {
        return claimedChunks.size();
    }

    public void loadClaims() {
        claimedChunks.clear();
        playerClaims.clear();

        FileConfiguration config = configManager.getClaimsConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");
        if (claimsSection == null) return;

        for (String playerIdStr : claimsSection.getKeys(false)) {
            UUID ownerId;
            try {
                ownerId = UUID.fromString(playerIdStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid player UUID: " + playerIdStr);
                continue;
            }

            List<String> chunkStrings = claimsSection.getStringList(playerIdStr);
            for (String chunkStr : chunkStrings) {
                String[] parts = chunkStr.split(",");
                if (parts.length != 3) {
                    plugin.getLogger().warning("Skipping invalid chunk entry: " + chunkStr);
                    continue;
                }

                try {
                    String world = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    ChunkPosition pos = new ChunkPosition(world, x, z);

                    claimedChunks.put(pos, ownerId);
                    playerClaims.computeIfAbsent(ownerId, k -> new HashSet<>()).add(pos);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Skipping chunk entry with invalid coordinates: " + chunkStr);
                }
            }
        }
    }

    public void saveClaims() {
        FileConfiguration config = configManager.getClaimsConfig();
        config.set("claims", null); // Clear existing claims

        ConfigurationSection claimsSection = config.createSection("claims");
        for (Map.Entry<UUID, Set<ChunkPosition>> entry : playerClaims.entrySet()) {
            List<String> chunkStrings = new ArrayList<>();
            for (ChunkPosition pos : entry.getValue()) {
                chunkStrings.add(pos.toString());
            }
            claimsSection.set(entry.getKey().toString(), chunkStrings);
        }

        configManager.saveClaimsConfig();
    }

    public boolean claimChunk(Player player, Chunk chunk) {
        // Check world restrictions
        String worldName = chunk.getWorld().getName();
        if (configManager.isWorldBlocked(worldName)) {
            player.sendMessage(ChatUtils.colorize(
                    configManager.getConfig().getString("messages.world-blocked", "&cYou cannot claim land in this world!")
            ));
            return false;
        }

        ChunkPosition pos = new ChunkPosition(chunk);
        if (isChunkClaimed(pos)) {
            UUID owner = getChunkOwner(pos);
            String ownerName = plugin.getServer().getOfflinePlayer(owner).getName();
            player.sendMessage(ChatUtils.colorize(
                    configManager.getConfig().getString("messages.already-claimed", "&cThis chunk is already claimed by {owner}!")
                            .replace("{owner}", ownerName != null ? ownerName : "Unknown")
            ));
            return false;
        }

        UUID playerId = player.getUniqueId();
        int claimLimit = getClaimLimit(player);
        Set<ChunkPosition> claims = playerClaims.getOrDefault(playerId, new HashSet<>());
        if (claims.size() >= claimLimit) {
            player.sendMessage(ChatUtils.colorize(
                    configManager.getConfig().getString("messages.claim-limit-reached", "&cYou've reached your claim limit of {limit} chunks!")
                            .replace("{limit}", String.valueOf(claimLimit))
            ));
            return false;
        }

        // Check if claim needs to be connected to existing claims
        if (configManager.requireConnectedClaims() && !claims.isEmpty()) {
            boolean isConnected = isConnectedToOwnClaims(pos, playerId);
            if (!isConnected) {
                player.sendMessage(ChatUtils.colorize(
                        configManager.getConfig().getString("messages.not-connected", "&cYou can only claim chunks adjacent to your existing claims!")
                ));
                return false;
            }
        }

        claimedChunks.put(pos, playerId);
        claims.add(pos);
        playerClaims.put(playerId, claims);
        return true;
    }

    private boolean isConnectedToOwnClaims(ChunkPosition pos, UUID playerId) {
        Set<ChunkPosition> claims = playerClaims.get(playerId);
        if (claims == null || claims.isEmpty()) return false;

        boolean allowDiagonals = configManager.allowDiagonalConnections();

        for (ChunkPosition neighbor : pos.getNeighbors(allowDiagonals)) {
            if (claims.contains(neighbor)) {
                return true;
            }
        }
        return false;
    }

    public boolean unclaimChunk(Chunk chunk) {
        ChunkPosition pos = new ChunkPosition(chunk);
        if (!isChunkClaimed(pos)) {
            return false;
        }

        UUID owner = claimedChunks.remove(pos);
        if (owner != null) {
            Set<ChunkPosition> claims = playerClaims.get(owner);
            if (claims != null) {
                claims.remove(pos);
                if (claims.isEmpty()) {
                    playerClaims.remove(owner);
                }
            }
            return true;
        }
        return false;
    }

    public boolean isChunkClaimed(ChunkPosition pos) {
        return claimedChunks.containsKey(pos);
    }

    public UUID getChunkOwner(ChunkPosition pos) {
        return claimedChunks.get(pos);
    }

    public Set<ChunkPosition> getPlayerClaims(UUID playerId) {
        return playerClaims.getOrDefault(playerId, Collections.emptySet());
    }

    public int getClaimLimit(Player player) {
        if (player.hasPermission("landclaim.admin")) return Integer.MAX_VALUE;

        // Check for specific limit permissions
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("landclaim.limit." + i)) return i;
        }

        return configManager.getConfig().getInt("chunk-claim-limit", 5);
    }
}