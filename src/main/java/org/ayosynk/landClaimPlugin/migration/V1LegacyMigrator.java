package org.ayosynk.landClaimPlugin.migration;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Two-step migrator for V1.
 * Step 1: preConfigCleanup - runs before Okaeri generates new configs to back
 * up V1 files.
 * Step 2: migrateClaims - runs after DatabaseManager init to move claims into
 * SQL.
 */
public class V1LegacyMigrator {

    // The file we read claims from during step 2
    private static final String BACKUP_CLAIMS_FILE = "old_claims.yml.v1-backup";

    /**
     * Call BEFORE ConfigManager initialization.
     * Backs up V1 config/messages/claims yml files, deletes all other yml files in
     * the data folder root.
     */
    public static void preConfigCleanup(LandClaimPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        File claimsFile = new File(dataFolder, "claims.yml");

        // If claims.yml doesn't exist, we assume V1 migration is not required (or
        // already done)
        if (!claimsFile.exists()) {
            return;
        }

        plugin.getLogger().info("=== V1 Legacy Data Cleanup ===");
        plugin.getLogger().info("Found claims.yml — preparing V1 file backups...");

        // Files we want to rename instead of delete
        Set<String> filesToBackup = Set.of("claims.yml", "config.yml", "messages.yml");

        File[] files = dataFolder.listFiles();
        if (files == null)
            return;

        int deletedCount = 0;
        for (File file : files) {
            // We only care about .yml files in the root folder, not directories (like
            // /locales or /menus)
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String name = file.getName();
                if (filesToBackup.contains(name)) {
                    File backup = new File(dataFolder, "old_" + name + ".v1-backup");
                    if (file.renameTo(backup)) {
                        plugin.getLogger().info("Backed up: " + name + " -> " + backup.getName());
                    } else {
                        plugin.getLogger().warning("Failed to backup: " + name);
                    }
                } else {
                    if (file.delete()) {
                        deletedCount++;
                    } else {
                        plugin.getLogger().warning("Failed to delete unused V1 config: " + name);
                    }
                }
            }
        }

        if (deletedCount > 0) {
            plugin.getLogger().info("Deleted " + deletedCount + " obsolete V1 root .yml files to prevent conflicts.");
        }
    }

    /**
     * Call AFTER DatabaseManager initialization.
     * Reads claims from old_claims.yml.v1-backup and saves to SQL.
     */
    public static void migrateClaims(LandClaimPlugin plugin) {
        File claimsBackup = new File(plugin.getDataFolder(), BACKUP_CLAIMS_FILE);

        // If the backup doesn't exist, migration either didn't happen or already
        // finished
        if (!claimsBackup.exists()) {
            return;
        }

        plugin.getLogger().info("=== V1 -> V2 Claims SQL Migration ===");
        plugin.getLogger().info("Starting legacy claims migration from " + BACKUP_CLAIMS_FILE + "...");

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(claimsBackup);
        ConfigurationSection claimsSection = yaml.getConfigurationSection("claims");

        if (claimsSection == null || claimsSection.getKeys(false).isEmpty()) {
            plugin.getLogger().info("Claims backup is empty or has no 'claims' section. Skipping migration.");
            renameToCompleted(plugin, claimsBackup);
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
                plugin.getLogger().warning("Skipping invalid UUID in claims: " + uuidString);
                continue;
            }

            // Skip if a profile already exists in V2
            ClaimProfile existing = plugin.getDatabaseManager().getProfileDao()
                    .getProfile(ownerId).join();
            if (existing != null) {
                skippedCount.incrementAndGet();
                continue;
            }

            // Resolve player name for the profile
            String playerName = Bukkit.getOfflinePlayer(ownerId).getName();
            if (playerName == null) {
                playerName = uuidString.substring(0, 8);
            }

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

            // Save to V2 database
            plugin.getDatabaseManager().getProfileDao().saveProfile(profile).join();
            profileCount.incrementAndGet();
        }

        plugin.getLogger().info("SQL Migration complete! Migrated " + profileCount.get()
                + " profiles with " + chunkCount.get() + " total chunks."
                + (skippedCount.get() > 0 ? " Skipped " + skippedCount.get() + " (already exist)." : ""));

        renameToCompleted(plugin, claimsBackup);
    }

    private static void renameToCompleted(LandClaimPlugin plugin, File file) {
        File finalBackup = new File(plugin.getDataFolder(), "old_claims.yml.v1-migrated");
        if (file.renameTo(finalBackup)) {
            plugin.getLogger().info("Renamed " + file.getName() + " -> " + finalBackup.getName());
        } else {
            plugin.getLogger().warning("Could not rename " + file.getName() + " to " + finalBackup.getName()
                    + ". Please rename manually to avoid re-migration.");
        }
    }
}
