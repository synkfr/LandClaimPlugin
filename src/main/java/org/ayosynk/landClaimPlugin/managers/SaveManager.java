package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages async and debounced save operations to prevent main thread blocking
 * and reduce disk I/O from frequent saves.
 */
public class SaveManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    
    // Debounce flags - mark data as dirty, save on next cycle
    private final AtomicBoolean claimsDirty = new AtomicBoolean(false);
    private final AtomicBoolean trustDirty = new AtomicBoolean(false);
    private final AtomicBoolean permissionsDirty = new AtomicBoolean(false);
    
    // Save interval in ticks (default: 60 seconds = 1200 ticks)
    private static final int SAVE_INTERVAL = 1200;
    
    public SaveManager(LandClaimPlugin plugin, ClaimManager claimManager, TrustManager trustManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
    }
    
    /**
     * Start the periodic save task
     */
    public void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveIfDirty();
            }
        }.runTaskTimerAsynchronously(plugin, SAVE_INTERVAL, SAVE_INTERVAL);
    }
    
    /**
     * Mark claims data as needing to be saved
     */
    public void markClaimsDirty() {
        claimsDirty.set(true);
    }
    
    /**
     * Mark trust data as needing to be saved
     */
    public void markTrustDirty() {
        trustDirty.set(true);
    }
    
    /**
     * Mark permissions data as needing to be saved
     */
    public void markPermissionsDirty() {
        permissionsDirty.set(true);
    }
    
    /**
     * Save all dirty data asynchronously
     */
    private void saveIfDirty() {
        if (claimsDirty.compareAndSet(true, false)) {
            // Run file I/O on async thread, but access Bukkit API on main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    claimManager.saveClaims();
                    if (plugin.getConfigManager().logAutoSaveMessage()) {
                        plugin.getLogger().info("Claims auto-saved.");
                    }
                }
            }.runTask(plugin);
        }
        
        if (trustDirty.compareAndSet(true, false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    trustManager.saveTrustedPlayers();
                    if (plugin.getConfigManager().logAutoSaveMessage()) {
                        plugin.getLogger().info("Trust data auto-saved.");
                    }
                }
            }.runTask(plugin);
        }
        
        if (permissionsDirty.compareAndSet(true, false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    trustManager.savePermissionsAndMembers();
                    if (plugin.getConfigManager().logAutoSaveMessage()) {
                        plugin.getLogger().info("Permissions auto-saved.");
                    }
                }
            }.runTask(plugin);
        }
    }
    
    /**
     * Force save all data immediately (for plugin disable)
     */
    public void saveAll() {
        claimManager.saveClaims();
        trustManager.saveTrustedPlayers();
        trustManager.savePermissionsAndMembers();
    }
    
    /**
     * Save all data asynchronously
     */
    public void saveAllAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAll();
            }
        }.runTaskAsynchronously(plugin);
    }
}
