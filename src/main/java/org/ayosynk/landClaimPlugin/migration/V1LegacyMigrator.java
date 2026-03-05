package org.ayosynk.landClaimPlugin.migration;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-shot migrator for V1 claims.yml → V2 SQL ClaimProfile system.
 *
 * V1 format (claims.yml):
 * claims:
 * <UUID>:
 * - "world,chunkX,chunkZ"
 *
 * After migration, claims.yml is renamed to claims.yml.v1-backup
 * to prevent re-execution.
 */
public class V1LegacyMigrator {

    public static void migrate(LandClaimPlugin plugin) {
        File claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!claimsFile.exists()) {
            return; // No V1 data — nothing to migrate
        }

        plugin.getLogger().info("=== V1 → V2 Migration ===");
        plugin.getLogger().info("Found claims.yml — starting legacy migration...");

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(claimsFile);
        ConfigurationSection claimsSection = yaml.getConfigurationSection("claims");

        if (claimsSection == null || claimsSection.getKeys(false).isEmpty()) {
            plugin.getLogger().info("claims.yml is empty or has no 'claims' section. Skipping migration.");
            renameFile(plugin, claimsFile);
            return;
        }

        AtomicInteger profileCount = new AtomicInteger(0);
        AtomicInteger chunkCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        for (String uuidString : claimsSection.getKeys(false)) {
            UUID ownerId;
            try {
                ownerId = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid UUID in claims.yml: " + uuidString);
                continue;
            }

            // Skip if a profile already exists in V2 (prevents double migration)
            ClaimProfile existing = plugin.getDatabaseManager().getProfileDao()
                    .getProfile(ownerId).join();
            if (existing != null) {
                skippedCount.incrementAndGet();
                continue;
            }

            // Resolve player name for the profile
            String playerName = Bukkit.getOfflinePlayer(ownerId).getName();
            if (playerName == null) {
                playerName = uuidString.substring(0, 8); // use first 8 chars of UUID
            }

            // Create a new V2 profile with default roles
            ClaimProfile profile = new ClaimProfile(ownerId, playerName + "'s Claim");

            // Parse V1 chunk entries: "world,chunkX,chunkZ"
            List<String> chunkEntries = claimsSection.getStringList(uuidString);
            for (String entry : chunkEntries) {
                String[] parts = entry.split(",");
                if (parts.length != 3) {
                    plugin.getLogger().warning("Skipping malformed chunk entry: " + entry);
                    continue;
                }
                try {
                    String world = parts[0].trim();
                    int chunkX = Integer.parseInt(parts[1].trim());
                    int chunkZ = Integer.parseInt(parts[2].trim());
                    profile.addChunk(new ChunkPosition(world, chunkX, chunkZ));
                    chunkCount.incrementAndGet();
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Skipping invalid chunk coordinates: " + entry);
                }
            }

            // Save to V2 database (synchronous join to ensure migration completes before
            // startup)
            plugin.getDatabaseManager().getProfileDao().saveProfile(profile).join();
            profileCount.incrementAndGet();
        }

        plugin.getLogger().info("Migration complete! Migrated " + profileCount.get()
                + " profiles with " + chunkCount.get() + " total chunks."
                + (skippedCount.get() > 0 ? " Skipped " + skippedCount.get() + " (already exist)." : ""));

        renameFile(plugin, claimsFile);
    }

    private static void renameFile(LandClaimPlugin plugin, File claimsFile) {
        File backup = new File(plugin.getDataFolder(), "claims.yml.v1-backup");
        if (claimsFile.renameTo(backup)) {
            plugin.getLogger().info("Renamed claims.yml → claims.yml.v1-backup");
        } else {
            plugin.getLogger().warning(
                    "Could not rename claims.yml. Please manually rename or delete it to prevent re-migration.");
        }
    }
}
