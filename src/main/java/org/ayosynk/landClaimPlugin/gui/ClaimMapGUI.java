package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
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
                if (!GuiHelper.checkMenuPermission(player, "map", plugin)) {
                        return;
                }
                // Capture the player's current chunk position synchronously (on the player's
                // region thread on Folia, or main thread on Paper). Reading
                // player.getLocation() here is safe.
                org.ayosynk.landClaimPlugin.models.ChunkPosition centerSync = new org.ayosynk.landClaimPlugin.models.ChunkPosition(
                                player.getLocation());
                final String world = centerSync.world();
                final int centerX = centerSync.x();
                final int centerZ = centerSync.z();

                FoliaScheduler.runAsync(plugin, () -> {
                        ClaimMapConfig config = plugin.getConfigManager().getClaimMapConfig();
                        org.ayosynk.landClaimPlugin.managers.ClaimManager claimManager = plugin.getClaimManager();

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
                                                                        // Folia: chunk operations need the
                                                                        // region's thread. Use runAtLocation
                                                                        // to dispatch safely.
                                                                        org.bukkit.World w = Bukkit.getWorld(world);
                                                                        if (w != null) {
                                                                                int bx = (chunkX << 4) + 8;
                                                                                int bz = (chunkZ << 4) + 8;
                                                                                org.bukkit.Location chunkLoc = new org.bukkit.Location(w, bx, 64, bz);
                                                                                FoliaScheduler.runAtLocation(plugin, chunkLoc, () -> {
                                                                                        org.bukkit.Chunk chunk = w.getChunkAt(chunkX, chunkZ);
                                                                                        if (claimManager.claimChunk(p, chunk)) {
                                                                                                open(p, profile, plugin);
                                                                                        }
                                                                                });
                                                                        }
                                                                });
                                        } else if (ownerProfile.getProfileId().equals(player.getUniqueId())) {
                                                // Your Claim
                                                String processedName = processText(config.yourClaim.name, ownerProfile, player);
                                                currentLore = processLore(config.yourClaim.lore, ownerProfile, player);
                                                slot = GuiHelper.buildSlot(config.yourClaim.material,
                                                                processedName, currentLore,
                                                                (p, e) -> {
                                                                        org.bukkit.World w = Bukkit.getWorld(world);
                                                                        if (w != null) {
                                                                                int bx = (chunkX << 4) + 8;
                                                                                int bz = (chunkZ << 4) + 8;
                                                                                org.bukkit.Location chunkLoc = new org.bukkit.Location(w, bx, 64, bz);
                                                                                FoliaScheduler.runAtLocation(plugin, chunkLoc, () -> {
                                                                                        org.bukkit.Chunk chunk = w.getChunkAt(chunkX, chunkZ);
                                                                                        if (claimManager.unclaimChunk(chunk)) {
                                                                                                open(p, profile, plugin);
                                                                                        }
                                                                                });
                                                                        }
                                                                });
                                        } else if (ownerProfile.isMember(player.getUniqueId())
                                                        || ownerProfile.isTrusted(player.getUniqueId())) {
                                                // Member/Trusted
                                                String processedName = processText(config.memberClaim.name, ownerProfile, player);
                                                currentLore = processLore(config.memberClaim.lore, ownerProfile, player);
                                                slot = GuiHelper.buildSlot(config.memberClaim.material,
                                                                processedName, currentLore);
                                        } else if (profile != null && profile.hasAlly(ownerProfile.getProfileId())) {
                                                // Ally
                                                String processedName = processText(config.allyClaim.name, ownerProfile, player);
                                                currentLore = processLore(config.allyClaim.lore, ownerProfile, player);
                                                slot = GuiHelper.buildSlot(config.allyClaim.material,
                                                                processedName, currentLore);
                                        } else {
                                                // Other
                                                String processedName = processText(config.otherClaim.name, ownerProfile, player);
                                                currentLore = processLore(config.otherClaim.lore, ownerProfile, player);
                                                slot = GuiHelper.buildSlot(config.otherClaim.material,
                                                                processedName, currentLore);
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

        private static String processText(String text, ClaimProfile ownerProfile, Player player) {
                if (text == null)
                        return null;
                if (ownerProfile == null)
                        return text;
                return GuiHelper.replacePlaceholders(text, ownerProfile, player, ownerProfile.getColoredOwnerName(), ownerProfile.getColoredName());
        }

        private static java.util.List<String> processLore(java.util.List<String> lore, ClaimProfile ownerProfile, Player player) {
                java.util.List<String> processed = new java.util.ArrayList<>();
                if (lore != null) {
                        for (String line : lore) {
                                processed.add(processText(line, ownerProfile, player));
                        }
                }
                return processed;
        }
}
