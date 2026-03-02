package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.MainMenuConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MainMenuGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        String ownerName = claim.getOwnerId() != null
                                        ? Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()
                                        : "Unknown";
                        if (ownerName == null)
                                ownerName = "Unknown";
                        String claimName = claim.getName() != null ? claim.getName() : "Unnamed Claim";

                        MainMenuConfig config = plugin.getConfigManager().getMainMenuConfig();

                        String[] structure = {
                                        "1 1 2 2 2 2 2 1 1",
                                        "1 M W A S T E V 1",
                                        "2 2 2 2 2 2 2 2 2",
                                        "2 2 2 1 X 1 2 2 2"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('1', GuiHelper.buildSlot(config.filler1.material, config.filler1.name,
                                        config.filler1.lore, claim, player, ownerName, claimName));
                        ingredients.put('2', GuiHelper.buildSlot(config.filler2.material, config.filler2.name,
                                        config.filler2.lore, claim, player, ownerName, claimName));
                        ingredients.put('M', GuiHelper.buildSlot(config.claimMap.material, config.claimMap.name,
                                        config.claimMap.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ClaimMapGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('W', GuiHelper.buildSlot(config.warps.material, config.warps.name,
                                        config.warps.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                WarpManagementGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('A', GuiHelper.buildSlot(config.allies.material, config.allies.name,
                                        config.allies.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                AllyManagementGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('S', GuiHelper.buildSlot(config.settings.material, config.settings.name,
                                        config.settings.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ClaimSettingsGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('T', GuiHelper.buildSlot(config.trusted.material, config.trusted.name,
                                        config.trusted.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                TrustManagementGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('E', GuiHelper.buildSlot(config.members.material, config.members.name,
                                        config.members.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                MemberManagementGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('V', GuiHelper.buildSlot(config.visitors.material, config.visitors.name,
                                        config.visitors.lore, claim, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                VisitorSettingsGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('X', GuiHelper.buildSlot(config.close.material, config.close.name,
                                        config.close.lore, claim, player, ownerName, claimName,
                                        (p, e) -> p.closeInventory()));

                        String windowTitle = config.title.replace("{claim_name}", claimName);
                        Component title = GuiHelper.MM.deserialize(windowTitle);

                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
