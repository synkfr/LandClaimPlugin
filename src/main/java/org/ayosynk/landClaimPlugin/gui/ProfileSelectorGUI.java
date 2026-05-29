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
        if (!GuiHelper.checkMenuPermission(player, "profiles", plugin)) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ClaimPlayer cp = plugin.getCacheManager().getPlayerCache().getIfPresent(player.getUniqueId());
            if (cp == null) return;
            
            org.ayosynk.landClaimPlugin.config.menus.ProfileSelectorConfig config = plugin.getConfigManager().getProfileSelectorConfig();
            
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
                        Material mat;
                        try {
                            mat = Material.valueOf(isActive ? config.activeProfileMaterial.toUpperCase() : config.inactiveProfileMaterial.toUpperCase());
                        } catch (Exception ex) {
                            mat = isActive ? Material.ENCHANTED_BOOK : Material.BOOK;
                        }
                        
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            String nameStr = config.profileNameFormat.replace("<name>", profile.getColoredName());
                            meta.displayName(GuiHelper.MM.deserialize(nameStr));
                            List<Component> lore = new ArrayList<>();
                            if (profile.isOwner(player.getUniqueId())) {
                                lore.add(GuiHelper.MM.deserialize(config.roleOwnerLore));
                            } else {
                                String role = profile.getMemberRole(player.getUniqueId());
                                lore.add(GuiHelper.MM.deserialize(config.roleMemberLore.replace("<role>", role != null ? role : "Member")));
                            }
                            lore.add(GuiHelper.MM.deserialize(config.chunksClaimedLore.replace("<count>", String.valueOf(profile.getOwnedChunks().size()))));
                            lore.add(GuiHelper.MM.deserialize(""));
                            if (isActive) {
                                lore.add(GuiHelper.MM.deserialize(config.activeProfileLore));
                            } else {
                                lore.add(GuiHelper.MM.deserialize(config.clickToActivateLore));
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
                                p.sendMessage(plugin.getConfigManager().getMessage("profile-changed", "<name>", profile.getColoredName()));
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
            ingredients.put('F', GuiHelper.buildSlot(config.filler.material, config.filler.name, config.filler.lore));
            ingredients.put('C', GuiHelper.buildSlot(config.close.material, config.close.name, config.close.lore, (p, e) -> p.closeInventory()));

            Component title = GuiHelper.MM.deserialize(config.title);
            PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

            gui.setPrevButton(39,
                    GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name, config.previousPage.lore),
                    GuiHelper.buildItemStack(config.filler.material, config.filler.name, config.filler.lore));
            gui.setNextButton(41,
                    GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name, config.nextPage.lore),
                    GuiHelper.buildItemStack(config.filler.material, config.filler.name, config.filler.lore));

            gui.setContent(contentItems, player);
            gui.open(player);
        });
    }
}
