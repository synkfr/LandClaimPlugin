package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapInfoConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClaimMapInfoGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        ClaimMapInfoConfig config = plugin.getConfigManager().getClaimMapInfoConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F O A P E W F F",
                                        "F F F F F F F F F",
                                        "F F F 1 B 1 F F F"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.filler.material, config.filler.name,
                                        config.filler.lore));
                        ingredients.put('1', GuiHelper.buildSlot(config.filler1.material, config.filler1.name,
                                        config.filler1.lore));
                        ingredients.put('O', GuiHelper.buildSlot(config.owned.material, config.owned.name,
                                        config.owned.lore));
                        ingredients.put('A',
                                        GuiHelper.buildSlot(config.ally.material, config.ally.name, config.ally.lore));
                        ingredients.put('P', GuiHelper.buildSlot(config.other.material, config.other.name,
                                        config.other.lore));
                        ingredients.put('E', GuiHelper.buildSlot(config.enemy.material, config.enemy.name,
                                        config.enemy.lore));
                        ingredients.put('W', GuiHelper.buildSlot(config.wilderness.material, config.wilderness.name,
                                        config.wilderness.lore));
                        ingredients.put('B',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimMapGUI.open(p, claim, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
