package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaveManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final HomeManager homeManager;

    private final AtomicBoolean claimsDirty = new AtomicBoolean(false);
    private final AtomicBoolean trustDirty = new AtomicBoolean(false);
    private final AtomicBoolean permissionsDirty = new AtomicBoolean(false);
    private final AtomicBoolean homesDirty = new AtomicBoolean(false);

    private static final int SAVE_INTERVAL = 1200;

    public SaveManager(LandClaimPlugin plugin, ClaimManager claimManager, TrustManager trustManager,
            HomeManager homeManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.homeManager = homeManager;
    }

    public void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveIfDirty();
            }
        }.runTaskTimerAsynchronously(plugin, SAVE_INTERVAL, SAVE_INTERVAL);
    }

    public void markClaimsDirty() {
        claimsDirty.set(true);
    }

    public void markTrustDirty() {
        trustDirty.set(true);
    }

    public void markPermissionsDirty() {
        permissionsDirty.set(true);
    }

    public void markHomesDirty() {
        homesDirty.set(true);
    }

    private void saveIfDirty() {
        if (claimsDirty.compareAndSet(true, false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    claimManager.saveClaims();
                    if (plugin.getConfigManager().logAutoSaveMessage())
                        plugin.getLogger().info("Claims auto-saved.");
                }
            }.runTask(plugin);
        }

        if (trustDirty.compareAndSet(true, false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    trustManager.saveTrustedPlayers();
                    if (plugin.getConfigManager().logAutoSaveMessage())
                        plugin.getLogger().info("Trust data auto-saved.");
                }
            }.runTask(plugin);
        }

        if (permissionsDirty.compareAndSet(true, false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    trustManager.savePermissionsAndMembers();
                    if (plugin.getConfigManager().logAutoSaveMessage())
                        plugin.getLogger().info("Permissions auto-saved.");
                }
            }.runTask(plugin);
        }

        if (homesDirty.compareAndSet(true, false)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    homeManager.save();
                    if (plugin.getConfigManager().logAutoSaveMessage())
                        plugin.getLogger().info("Homes auto-saved.");
                }
            }.runTask(plugin);
        }
    }

    public void saveAll() {
        claimManager.saveClaims();
        trustManager.saveTrustedPlayers();
        trustManager.savePermissionsAndMembers();
        homeManager.save();
    }

    public void saveAllAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAll();
            }
        }.runTaskAsynchronously(plugin);
    }
}
