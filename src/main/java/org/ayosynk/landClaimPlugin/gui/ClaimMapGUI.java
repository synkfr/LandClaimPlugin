package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClaimMapGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        ClaimMapConfig config = plugin.getConfigManager().getClaimMapConfig();
                        org.ayosynk.landClaimPlugin.managers.ClaimManager claimManager = plugin.getClaimManager();
                        org.ayosynk.landClaimPlugin.models.ChunkPosition center = new org.ayosynk.landClaimPlugin.models.ChunkPosition(
                                        player.getLocation());
                        String world = center.world();
                        int centerX = center.x();
                        int centerZ = center.z();

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 6);

                        // Row 0-4: 9x5 Map Grid
                        for (int row = 0; row < 5; row++) {
                                for (int col = 0; col < 9; col++) {
                                        int chunkX = centerX + (col - 4);
                                        int chunkZ = centerZ + (row - 2);
                                        org.ayosynk.landClaimPlugin.models.ChunkPosition pos = new org.ayosynk.landClaimPlugin.models.ChunkPosition(
                                                        world, chunkX, chunkZ);

                                        ClaimProfile ownerProfile = claimManager.getProfileAt(pos);
                                        SlotDefinition slot;
                                        java.util.List<String> currentLore;

                                        if (ownerProfile == null) {
                                                // Wilderness
                                                currentLore = config.wilderness.lore;
                                                slot = GuiHelper.buildSlot(config.wilderness.material,
                                                                config.wilderness.name, currentLore,
                                                                (p, e) -> {
                                                                        org.bukkit.World w = Bukkit.getWorld(world);
                                                                        if (w != null) {
                                                                                org.bukkit.Chunk chunk = w
                                                                                                .getChunkAtAsync(chunkX,
                                                                                                                chunkZ)
                                                                                                .join();
                                                                                if (claimManager.claimChunk(p, chunk)) {
                                                                                        open(p, profile, plugin);
                                                                                }
                                                                        }
                                                                });
                                        } else if (ownerProfile.getOwnerId().equals(player.getUniqueId())) {
                                                // Your Claim
                                                currentLore = config.yourClaim.lore;
                                                slot = GuiHelper.buildSlot(config.yourClaim.material,
                                                                config.yourClaim.name, currentLore,
                                                                (p, e) -> {
                                                                        org.bukkit.World w = Bukkit.getWorld(world);
                                                                        if (w != null) {
                                                                                org.bukkit.Chunk chunk = w
                                                                                                .getChunkAtAsync(chunkX,
                                                                                                                chunkZ)
                                                                                                .join();
                                                                                if (claimManager.unclaimChunk(chunk)) {
                                                                                        open(p, profile, plugin);
                                                                                }
                                                                        }
                                                                });
                                        } else if (ownerProfile.isMember(player.getUniqueId())
                                                        || ownerProfile.isTrusted(player.getUniqueId())) {
                                                // Member/Trusted
                                                String ownerName = Bukkit.getOfflinePlayer(ownerProfile.getOwnerId())
                                                                .getName();
                                                currentLore = processLore(config.memberClaim.lore, ownerName);
                                                slot = GuiHelper.buildSlot(config.memberClaim.material,
                                                                config.memberClaim.name, currentLore);
                                        } else if (profile != null && profile.hasAlly(ownerProfile.getOwnerId())) {
                                                // Ally
                                                String ownerName = Bukkit.getOfflinePlayer(ownerProfile.getOwnerId())
                                                                .getName();
                                                currentLore = processLore(config.allyClaim.lore, ownerName);
                                                slot = GuiHelper.buildSlot(config.allyClaim.material,
                                                                config.allyClaim.name, currentLore);
                                        } else {
                                                // Other
                                                String ownerName = Bukkit.getOfflinePlayer(ownerProfile.getOwnerId())
                                                                .getName();
                                                currentLore = processLore(config.otherClaim.lore, ownerName);
                                                slot = GuiHelper.buildSlot(config.otherClaim.material,
                                                                config.otherClaim.name, currentLore);
                                        }

                                        // If this is the player's current chunk, highlight it or add indicator
                                        if (chunkX == centerX && chunkZ == centerZ) {
                                                slot = GuiHelper.buildSlot(config.currentPos.material,
                                                                config.currentPos.name, currentLore,
                                                                slot.action());
                                        }

                                        gui.setItem(row * 9 + col, slot.item(), slot.action());
                                }
                        }

                        // Row 5: Bottom Fill + Controls
                        for (int i = 45; i < 54; i++) {
                                gui.setItem(i, GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name,
                                                config.bottomFill.lore).item());
                        }

                        SlotDefinition refreshSlot = GuiHelper.buildSlot(config.refresh.material, config.refresh.name,
                                        config.refresh.lore, (p, e) -> {
                                                open(p, profile, plugin);
                                        });
                        gui.setItem(47, refreshSlot.item(), refreshSlot.action());

                        SlotDefinition backSlot = GuiHelper.buildSlot(config.back.material, config.back.name,
                                        config.back.lore, (p, e) -> {
                                                p.closeInventory();
                                                MainMenuGUI.open(p, profile, plugin);
                                        });
                        gui.setItem(49, backSlot.item(), backSlot.action());

                        SlotDefinition infoSlot = GuiHelper.buildSlot(config.info.material, config.info.name,
                                        config.info.lore, (p, e) -> {
                                                p.closeInventory();
                                                ClaimMapInfoGUI.open(p, profile, plugin);
                                        });
                        gui.setItem(51, infoSlot.item(), infoSlot.action());

                        gui.open(player);
                });
        }

        private static java.util.List<String> processLore(java.util.List<String> lore, String owner) {
                if (owner == null)
                        owner = "Unknown";
                java.util.List<String> processed = new java.util.ArrayList<>();
                for (String line : lore) {
                        processed.add(line.replace("{owner}", owner));
                }
                return processed;
        }
}
