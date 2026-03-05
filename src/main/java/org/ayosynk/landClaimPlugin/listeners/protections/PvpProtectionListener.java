package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class PvpProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public PvpProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    private boolean isPvpAllowed(Location location) {
        ChunkPosition pos = new ChunkPosition(location);
        if (claimManager.isChunkClaimed(pos)) {
            // Future implementation: check if the claim has a specific flag overriding PVP.
            // For now, by default, PvP is completely disabled in claims unless a specific
            // role flag allows it
            // (or if we strictly enforce no-pvp 24/7 as requested).
            return false;
        }
        return true; // PvP is allowed in wilderness
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player victim = (Player) event.getEntity();
        Player attacker = null;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker == null || attacker.equals(victim))
            return;

        if (!isPvpAllowed(victim.getLocation()) || !isPvpAllowed(attacker.getLocation())) {
            event.setCancelled(true);
            attacker.sendMessage(configManager.getMessage("pvp-denied"));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (!(potion.getShooter() instanceof Player))
            return;

        Player thrower = (Player) potion.getShooter();

        // Check each affected entity
        event.getAffectedEntities().forEach(entity -> {
            if (entity instanceof Player && !entity.equals(thrower)) {
                // If the victim is in a claim where PvP is disabled, or thrower is in claim
                if (!isPvpAllowed(entity.getLocation()) || !isPvpAllowed(thrower.getLocation())) {
                    event.setIntensity(entity, 0); // Nullify effect on this specific player
                }
            }
        });
    }
}
