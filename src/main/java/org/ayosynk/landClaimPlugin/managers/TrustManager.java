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

    private final Map<UUID, Map<UUID, Set<String>>> trustPermissions = new HashMap<>(); // Owner -> Trusted Player -> Permissions
    private final Map<UUID, Map<String, Boolean>> visitorPermissions = new HashMap<>(); // Owner -> Permission -> Enabled
    private final Map<UUID, Set<UUID>> claimMembers = new HashMap<>(); // Owner -> Members

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

    public void initialize() {
        loadTrustedPlayers();
        loadPermissions();
        loadMembers();
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

    public void loadPermissions() {
        trustPermissions.clear();
        visitorPermissions.clear();

        FileConfiguration config = configManager.getTrustConfig();
        ConfigurationSection permissionsSection = config.getConfigurationSection("permissions");
        if (permissionsSection == null) return;

        for (String ownerIdStr : permissionsSection.getKeys(false)) {
            UUID owner = UUID.fromString(ownerIdStr);
            ConfigurationSection ownerSection = permissionsSection.getConfigurationSection(ownerIdStr);

            ConfigurationSection trustSection = ownerSection.getConfigurationSection("trust");
            if (trustSection != null) {
                for (String trustedIdStr : trustSection.getKeys(false)) {
                    UUID trustedId = UUID.fromString(trustedIdStr);
                    Set<String> permissions = new HashSet<>(trustSection.getStringList(trustedIdStr));
                    trustPermissions.computeIfAbsent(owner, k -> new HashMap<>()).put(trustedId, permissions);
                }
            }

            ConfigurationSection visitorSection = ownerSection.getConfigurationSection("visitor");
            if (visitorSection != null) {
                Map<String, Boolean> perms = new HashMap<>();
                for (String perm : visitorSection.getKeys(false)) {
                    perms.put(perm, visitorSection.getBoolean(perm));
                }
                visitorPermissions.put(owner, perms);
            }
        }
    }

    public void loadMembers() {
        claimMembers.clear();
        FileConfiguration config = configManager.getTrustConfig();
        ConfigurationSection membersSection = config.getConfigurationSection("members");
        if (membersSection == null) return;

        for (String ownerIdStr : membersSection.getKeys(false)) {
            UUID owner = UUID.fromString(ownerIdStr);
            List<String> memberIds = membersSection.getStringList(ownerIdStr);
            Set<UUID> memberSet = new HashSet<>();
            for (String id : memberIds) {
                memberSet.add(UUID.fromString(id));
            }
            claimMembers.put(owner, memberSet);
        }
    }

    public void saveTrustedPlayers() {
        FileConfiguration config = configManager.getTrustConfig();
        config.set("trust", null);

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

    public void savePermissionsAndMembers() {
        FileConfiguration config = configManager.getTrustConfig();

        // Save permissions
        config.set("permissions", null);
        ConfigurationSection permissionsSection = config.createSection("permissions");

        for (Map.Entry<UUID, Map<UUID, Set<String>>> ownerEntry : trustPermissions.entrySet()) {
            ConfigurationSection ownerSection = permissionsSection.createSection(ownerEntry.getKey().toString());
            ConfigurationSection trustSection = ownerSection.createSection("trust");

            for (Map.Entry<UUID, Set<String>> trustEntry : ownerEntry.getValue().entrySet()) {
                trustSection.set(trustEntry.getKey().toString(), new ArrayList<>(trustEntry.getValue()));
            }
        }

        for (Map.Entry<UUID, Map<String, Boolean>> ownerEntry : visitorPermissions.entrySet()) {
            ConfigurationSection ownerSection = permissionsSection.getConfigurationSection(ownerEntry.getKey().toString());
            if (ownerSection == null) {
                ownerSection = permissionsSection.createSection(ownerEntry.getKey().toString());
            }

            ConfigurationSection visitorSection = ownerSection.createSection("visitor");
            for (Map.Entry<String, Boolean> permEntry : ownerEntry.getValue().entrySet()) {
                visitorSection.set(permEntry.getKey(), permEntry.getValue());
            }
        }

        config.set("members", null);
        ConfigurationSection membersSection = config.createSection("members");

        for (Map.Entry<UUID, Set<UUID>> entry : claimMembers.entrySet()) {
            List<String> memberIds = new ArrayList<>();
            for (UUID id : entry.getValue()) {
                memberIds.add(id.toString());
            }
            membersSection.set(entry.getKey().toString(), memberIds);
        }

        configManager.saveTrustConfig();
    }

    public boolean addTrustedPlayer(Player owner, String targetName) {
        if (owner.getName().equalsIgnoreCase(targetName)) {
            return false;
        }

        Player onlinePlayer = Bukkit.getPlayerExact(targetName);
        if (onlinePlayer != null) {
            return addTrustedPlayer(owner, onlinePlayer.getUniqueId());
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null &&
                    offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                return addTrustedPlayer(owner, offlinePlayer.getUniqueId());
            }
        }

        return false;
    }

    public boolean addTrustedPlayer(Player owner, UUID trustedId) {
        UUID ownerId = owner.getUniqueId();
        trustedPlayers.computeIfAbsent(ownerId, k -> new HashSet<>()).add(trustedId);

        setDefaultPermissions(ownerId, trustedId);
        return true;
    }

    private void setDefaultPermissions(UUID ownerId, UUID trustedId) {
        for (String permission : new String[]{"BUILD", "INTERACT", "CONTAINER", "TELEPORT"}) {
            if (configManager.getDefaultTrustPermission(permission)) {
                setTrustPermission(ownerId, trustedId, permission, true);
            }
        }
    }

    public boolean removeTrustedPlayer(Player owner, String targetName) {
        Player onlinePlayer = Bukkit.getPlayerExact(targetName);
        if (onlinePlayer != null) {
            return removeTrustedPlayer(owner, onlinePlayer.getUniqueId());
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null &&
                    offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                return removeTrustedPlayer(owner, offlinePlayer.getUniqueId());
            }
        }

        return false;
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

    public boolean addMember(UUID ownerId, OfflinePlayer member) {
        claimMembers.computeIfAbsent(ownerId, k -> new HashSet<>()).add(member.getUniqueId());
        return true;
    }

    public boolean removeMember(UUID ownerId, OfflinePlayer member) {
        Set<UUID> members = claimMembers.get(ownerId);
        if (members != null) {
            return members.remove(member.getUniqueId());
        }
        return false;
    }

    public boolean isMember(UUID ownerId, Player player) {
        Set<UUID> members = claimMembers.get(ownerId);
        return members != null && members.contains(player.getUniqueId());
    }

    public Set<UUID> getMembers(UUID ownerId) {
        return claimMembers.getOrDefault(ownerId, Collections.emptySet());
    }

    public void setTrustPermission(UUID ownerId, UUID trustedId, String permission, boolean enabled) {
        Map<UUID, Set<String>> ownerPermissions = trustPermissions.computeIfAbsent(ownerId, k -> new HashMap<>());
        Set<String> permissions = ownerPermissions.computeIfAbsent(trustedId, k -> new HashSet<>());

        if (enabled) {
            permissions.add(permission);
        } else {
            permissions.remove(permission);
        }
    }

    public boolean hasTrustPermission(UUID ownerId, UUID trustedId, String permission) {
        Map<UUID, Set<String>> ownerPermissions = trustPermissions.get(ownerId);
        if (ownerPermissions == null) {
            return configManager.getDefaultTrustPermission(permission);
        }

        Set<String> permissions = ownerPermissions.get(trustedId);
        return permissions != null && permissions.contains(permission);
    }

    public void setVisitorPermission(UUID ownerId, String permission, boolean enabled) {
        Map<String, Boolean> permissions = visitorPermissions.computeIfAbsent(ownerId, k -> new HashMap<>());
        permissions.put(permission, enabled);
    }

    public boolean hasVisitorPermission(UUID ownerId, String permission) {
        Map<String, Boolean> permissions = visitorPermissions.get(ownerId);
        if (permissions == null) {
            return configManager.getDefaultVisitorPermission(permission);
        }
        return permissions.getOrDefault(permission, false);
    }

    public boolean canManageTrust(UUID ownerId, Player player) {
        return player.getUniqueId().equals(ownerId) || isMember(ownerId, player);
    }
}