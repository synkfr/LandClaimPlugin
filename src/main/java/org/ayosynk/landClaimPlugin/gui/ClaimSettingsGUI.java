package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ClaimSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClaimSettingsGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        String ownerName = claim.getOwnerId() != null
                                        ? Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()
                                        : "Unknown";
                        if (ownerName == null)
                                ownerName = "Unknown";
                        String claimName = claim.getName() != null ? claim.getName() : "Unnamed Claim";

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
                                        config.filler1.lore, claim, player, ownerName, claimName));
                        ingredients.put('2', GuiHelper.buildSlot(config.filler2.material, config.filler2.name,
                                        config.filler2.lore, claim, player, ownerName, claimName));
                        ingredients.put('O', GuiHelper.buildSlot(config.overview.material, config.overview.name,
                                        config.overview.lore, claim, player, ownerName, claimName));
                        ingredients.put('N', GuiHelper.buildSlot(config.rename.material, config.rename.name,
                                        config.rename.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                RenameClaimGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('C', GuiHelper.buildSlot(config.color.material, config.color.name,
                                        config.color.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ChangeClaimColorGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('R', GuiHelper.buildSlot(config.roles.material, config.roles.name,
                                        config.roles.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                RoleManagementGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('W', GuiHelper.buildSlot(config.warps.material, config.warps.name,
                                        config.warps.lore, claim, player, ownerName, claimName));
                        ingredients.put('V', GuiHelper.buildSlot(config.visibility.material, config.visibility.name,
                                        config.visibility.lore, claim, player, ownerName, claimName));
                        ingredients.put('T', GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                                        config.titleToggle.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                TitleToggleGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('A', GuiHelper.buildSlot(config.abandonAll.material, config.abandonAll.name,
                                        config.abandonAll.lore, claim, player, ownerName, claimName));
                        ingredients.put('B', GuiHelper.buildSlot(config.back.material, config.back.name,
                                        config.back.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                MainMenuGUI.open(p, claim, plugin);
                                        }));

                        String windowTitle = config.title.replace("{claim_name}", claimName);
                        Component title = GuiHelper.MM.deserialize(windowTitle);

                        CustomGui gui = new CustomGui(title, 5);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
