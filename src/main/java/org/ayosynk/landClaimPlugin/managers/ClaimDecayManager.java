package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.api.event.ClaimDeleteEvent;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClaimDecayManager {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ClaimDecayManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    public void start() {
        if (!configManager.getPluginConfig().decay.enabled) {
            return;
        }

        long checkIntervalHours = Math.max(1, configManager.getPluginConfig().decay.checkIntervalHours);
        long intervalTicks = checkIntervalHours * 3600L * 20L;
        long initialDelayTicks = 300L * 20L;

        FoliaScheduler.runTaskTimer(plugin, () -> {
            runDecayCheck(null);
        }, initialDelayTicks, intervalTicks);
    }

    public void runDecayCheck(CommandSender notifySender) {
        if (isRunning.getAndSet(true)) {
            if (notifySender != null) {
                notifySender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>A claim decay scan is already in progress."));
            }
            return;
        }

        if (notifySender != null) {
            notifySender.sendMessage(configManager.getMessage("admin-decay-run-started"));
        }

        FoliaScheduler.runAsync(plugin, () -> {
            try {
                int inactiveDaysConfig = configManager.getPluginConfig().decay.inactiveDays;
                boolean onlyCheckOwner = configManager.getPluginConfig().decay.onlyCheckOwner;
                String exemptPermission = configManager.getPluginConfig().decay.exemptPermission;

                int decayedClaims = 0;
                int freedChunks = 0;

                for (ClaimProfile profile : claimManager.getAllProfiles()) {
                    if (profile == null) continue;
                    UUID ownerId = profile.getOwnerId();
                    if (ownerId == null || ownerId.equals(ClaimProfile.ADMIN_PROFILE_ID)) {
                        continue;
                    }

                    if (isExempt(ownerId, exemptPermission)) {
                        continue;
                    }

                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
                    if (owner.isOnline()) {
                        continue;
                    }

                    long lastSeen = getPlayerLastSeen(owner);
                    if (lastSeen <= 0) {
                        continue;
                    }

                    long inactiveMillis = System.currentTimeMillis() - lastSeen;
                    long inactiveDays = TimeUnit.MILLISECONDS.toDays(inactiveMillis);

                    if (inactiveDays < inactiveDaysConfig) {
                        continue;
                    }

                    if (!onlyCheckOwner) {
                        boolean activeMemberFound = false;
                        for (UUID memberId : profile.getMemberRoles().keySet()) {
                            if (memberId.equals(ownerId)) continue;
                            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
                            if (member.isOnline()) {
                                activeMemberFound = true;
                                break;
                            }
                            long memberLastSeen = getPlayerLastSeen(member);
                            if (memberLastSeen > 0 && TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - memberLastSeen) < inactiveDaysConfig) {
                                activeMemberFound = true;
                                break;
                            }
                        }
                        if (activeMemberFound) {
                            continue;
                        }
                    }

                    int chunks = claimManager.deleteProfile(profile.getProfileId(), ClaimDeleteEvent.DeleteReason.INACTIVITY_DECAY);
                    decayedClaims++;
                    freedChunks += chunks;

                    String ownerName = owner.getName() != null ? owner.getName() : ownerId.toString();
                    plugin.getLogger().info("Decayed claim '" + profile.getName() + "' belonging to inactive player " + ownerName + " (offline for " + inactiveDays + " days). Freed " + chunks + " chunks.");
                }

                if (notifySender != null) {
                    notifySender.sendMessage(configManager.getMessage("admin-decay-run-finished",
                            "<claims>", String.valueOf(decayedClaims),
                            "<chunks>", String.valueOf(freedChunks)));
                }

                if (decayedClaims > 0) {
                    plugin.getLogger().info("Claim decay sweep finished. " + decayedClaims + " claims decayed, " + freedChunks + " chunks freed.");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error occurred during claim decay check: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isRunning.set(false);
            }
        });
    }

    public boolean isExempt(UUID playerId, String exemptPermission) {
        ClaimPlayer cp = plugin.getCacheManager().getPlayerCache().getIfPresent(playerId);
        if (cp == null) {
            try {
                cp = plugin.getDatabaseManager().getPlayerDao().getPlayer(playerId).join();
            } catch (Exception ignored) {}
        }
        if (cp != null && cp.isDecayExempt()) {
            return true;
        }

        Player online = Bukkit.getPlayer(playerId);
        if (online != null && exemptPermission != null && !exemptPermission.isBlank()) {
            return online.hasPermission(exemptPermission);
        }

        return false;
    }

    public void setExempt(UUID playerId, boolean exempt) {
        ClaimPlayer cp = plugin.getCacheManager().getPlayerCache().getIfPresent(playerId);
        if (cp == null) {
            try {
                cp = plugin.getDatabaseManager().getPlayerDao().getPlayer(playerId).join();
            } catch (Exception ignored) {}
        }
        if (cp == null) {
            cp = new ClaimPlayer(playerId);
        }
        cp.setDecayExempt(exempt);
        plugin.getDatabaseManager().getPlayerDao().savePlayer(cp).join();
        plugin.getCacheManager().getPlayerCache().put(playerId, cp);
    }

    private long getPlayerLastSeen(OfflinePlayer player) {
        long lastPlayed = player.getLastPlayed();
        if (lastPlayed > 0) {
            return lastPlayed;
        }
        try {
            Method m = player.getClass().getMethod("getLastLogin");
            Object val = m.invoke(player);
            if (val instanceof Long l && l > 0) {
                return l;
            }
        } catch (Throwable ignored) {}
        return 0L;
    }
}
