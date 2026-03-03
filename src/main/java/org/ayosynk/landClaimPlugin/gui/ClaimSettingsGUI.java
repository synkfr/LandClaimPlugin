package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ClaimSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClaimSettingsGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        String ownerName = profile.getOwnerId() != null
                                        ? Bukkit.getOfflinePlayer(profile.getOwnerId()).getName()
                                        : "Unknown";
                        if (ownerName == null)
                                ownerName = "Unknown";
                        String claimName = profile.getName() != null ? profile.getName() : "Unnamed Claim";

                        ClaimSettingsConfig config = plugin.getConfigManager().getClaimSettingsConfig();

                        String[] structure = {
                                        "1 1 2 2 O 2 2 1 1",
                                        "1 2 2 2 2 2 2 2 1",
                                        "2 N C R W V T A 2",
                                        "1 2 2 2 2 2 2 2 1",
                                        "1 1 2 2 B 2 2 1 1"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('1', GuiHelper.buildSlot(config.filler1.material, config.filler1.name,
                                        config.filler1.lore, profile, player, ownerName, claimName));
                        ingredients.put('2', GuiHelper.buildSlot(config.filler2.material, config.filler2.name,
                                        config.filler2.lore, profile, player, ownerName, claimName));
                        ingredients.put('O', GuiHelper.buildSlot(config.overview.material, config.overview.name,
                                        config.overview.lore, profile, player, ownerName, claimName));
                        ingredients.put('N', GuiHelper.buildSlot(config.rename.material, config.rename.name,
                                        config.rename.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                RenameClaimGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('C', GuiHelper.buildSlot(config.color.material, config.color.name,
                                        config.color.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ChangeClaimColorGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('R', GuiHelper.buildSlot(config.roles.material, config.roles.name,
                                        config.roles.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                RoleManagementGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('W', GuiHelper.buildSlot(config.warps.material, config.warps.name,
                                        config.warps.lore, profile, player, ownerName, claimName));
                        ingredients.put('V', GuiHelper.buildSlot(config.visibility.material, config.visibility.name,
                                        config.visibility.lore, profile, player, ownerName, claimName));
                        ingredients.put('T', GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                                        config.titleToggle.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                TitleToggleGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('A', GuiHelper.buildSlot(config.abandonAll.material, config.abandonAll.name,
                                        config.abandonAll.lore, profile, player, ownerName, claimName));
                        ingredients.put('B', GuiHelper.buildSlot(config.back.material, config.back.name,
                                        config.back.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                MainMenuGUI.open(p, profile, plugin);
                                        }));

                        String windowTitle = config.title.replace("{claim_name}", claimName);
                        Component title = GuiHelper.MM.deserialize(windowTitle);

                        CustomGui gui = new CustomGui(title, 5);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
