package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class TrustManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;
    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>(); // Owner -> Trusted Players

    public TrustManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    public void initialize() {
        loadTrustedPlayers();
    }

    public int getTotalTrusts() {
        int count = 0;
        for (Set<UUID> trustedSet : trustedPlayers.values()) {
            count += trustedSet.size();
        }
        return count;
    }

    public void loadTrustedPlayers() {
        trustedPlayers.clear();
        FileConfiguration config = configManager.getTrustConfig();
        ConfigurationSection trustSection = config.getConfigurationSection("trust");
        if (trustSection == null) return;

        for (String ownerIdStr : trustSection.getKeys(false)) {
            try {
                UUID owner = UUID.fromString(ownerIdStr);
                List<String> trustedIds = trustSection.getStringList(ownerIdStr);
                Set<UUID> trustedSet = new HashSet<>();
                for (String id : trustedIds) {
                    trustedSet.add(UUID.fromString(id));
                }
                trustedPlayers.put(owner, trustedSet);
            } catch (Exception e) {
                plugin.getLogger().warning("Skipping invalid trust entry: " + ownerIdStr);
            }
        }
    }

    public void saveTrustedPlayers() {
        FileConfiguration config = configManager.getTrustConfig();
        config.set("trust", null); // Clear existing trust data

        ConfigurationSection trustSection = config.createSection("trust");
        for (Map.Entry<UUID, Set<UUID>> entry : trustedPlayers.entrySet()) {
            List<String> trustedIds = new ArrayList<>();
            for (UUID id : entry.getValue()) {
                trustedIds.add(id.toString());
            }
            trustSection.set(entry.getKey().toString(), trustedIds);
        }

        configManager.saveTrustConfig();
    }

    public boolean addTrustedPlayer(Player owner, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || !target.hasPlayedBefore()) {
            return false;
        }
        return addTrustedPlayer(owner, target.getUniqueId());
    }

    public boolean addTrustedPlayer(Player owner, UUID trustedId) {
        UUID ownerId = owner.getUniqueId();
        trustedPlayers.computeIfAbsent(ownerId, k -> new HashSet<>()).add(trustedId);
        return true;
    }

    public boolean removeTrustedPlayer(Player owner, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || !target.hasPlayedBefore()) {
            return false;
        }
        return removeTrustedPlayer(owner, target.getUniqueId());
    }

    public boolean removeTrustedPlayer(Player owner, UUID trustedId) {
        UUID ownerId = owner.getUniqueId();
        Set<UUID> trustedSet = trustedPlayers.get(ownerId);
        if (trustedSet != null) {
            return trustedSet.remove(trustedId);
        }
        return false;
    }

    public Set<UUID> getTrustedPlayers(UUID ownerId) {
        return trustedPlayers.getOrDefault(ownerId, Collections.emptySet());
    }

    public boolean isTrusted(UUID ownerId, Player player) {
        return getTrustedPlayers(ownerId).contains(player.getUniqueId());
    }
}