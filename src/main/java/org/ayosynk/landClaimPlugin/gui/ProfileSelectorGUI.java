package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSelectorGUI {

    public static void open(Player player, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ClaimPlayer cp = plugin.getCacheManager().getPlayerCache().getIfPresent(player.getUniqueId());
            if (cp == null) return;
            
            List<ClaimProfile> ownedProfiles = plugin.getClaimManager().getOwnedProfiles(player.getUniqueId());
            
            // Also add member profiles
            List<ClaimProfile> memberProfiles = plugin.getClaimManager().getMemberProfiles(player.getUniqueId());
            
            List<ClaimProfile> allProfiles = new ArrayList<>(ownedProfiles);
            for (ClaimProfile mp : memberProfiles) {
                if (!allProfiles.contains(mp)) {
                    allProfiles.add(mp);
                }
            }

            List<GuiItem> contentItems = new ArrayList<>();

            for (ClaimProfile profile : allProfiles) {
                boolean isActive = cp.getActiveProfileId() != null && cp.getActiveProfileId().equals(profile.getProfileId());
                
                contentItems.add(new GuiItem() {
                    @Override
                    public ItemStack render(Player viewer) {
                        ItemStack item = new ItemStack(isActive ? Material.ENCHANTED_BOOK : Material.BOOK);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(GuiHelper.MM.deserialize("<gold><bold>" + profile.getName()));
                            List<Component> lore = new ArrayList<>();
                            if (profile.isOwner(player.getUniqueId())) {
                                lore.add(GuiHelper.MM.deserialize("<gray>Role: <yellow>Owner"));
                            } else {
                                String role = profile.getMemberRole(player.getUniqueId());
                                lore.add(GuiHelper.MM.deserialize("<gray>Role: <yellow>" + (role != null ? role : "Member")));
                            }
                            lore.add(GuiHelper.MM.deserialize("<gray>Chunks Claimed: <yellow>" + profile.getOwnedChunks().size()));
                            lore.add(GuiHelper.MM.deserialize(""));
                            if (isActive) {
                                lore.add(GuiHelper.MM.deserialize("<green>Currently Active Profile"));
                            } else {
                                lore.add(GuiHelper.MM.deserialize("<yellow>Click to set as Active Profile"));
                            }
                            meta.lore(lore);
                            item.setItemMeta(meta);
                        }
                        return item;
                    }

                    @Override
                    public ClickAction clickAction() {
                        return (p, e) -> {
                            if (!isActive) {
                                cp.setActiveProfileId(profile.getProfileId());
                                plugin.getDatabaseManager().getPlayerDao().savePlayer(cp);
                                p.sendMessage(plugin.getConfigManager().getMessage("profile-changed", "<name>", profile.getName()));
                            }
                            p.closeInventory();
                            // Re-open main menu with the new active profile
                            MainMenuGUI.open(p, profile, plugin);
                        };
                    }
                });
            }

            String[] structure = {
                    "F F F F F F F F F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "F F F P C N F F F"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot("GRAY_STAINED_GLASS_PANE", "<gray> ", new ArrayList<>()));
            ingredients.put('C', GuiHelper.buildSlot("BARRIER", "<red>Close", new ArrayList<>(), (p, e) -> p.closeInventory()));

            Component title = GuiHelper.MM.deserialize("<dark_gray>Select Claim Profile");
            PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

            gui.setPrevButton(39,
                    GuiHelper.buildItemStack("ARROW", "<green>Previous Page", new ArrayList<>()),
                    GuiHelper.buildItemStack("GRAY_STAINED_GLASS_PANE", "<gray> ", new ArrayList<>()));
            gui.setNextButton(41,
                    GuiHelper.buildItemStack("ARROW", "<green>Next Page", new ArrayList<>()),
                    GuiHelper.buildItemStack("GRAY_STAINED_GLASS_PANE", "<gray> ", new ArrayList<>()));

            gui.setContent(contentItems, player);
            gui.open(player);
        });
    }
}
