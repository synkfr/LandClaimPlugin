package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClaimMapGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        ClaimMapConfig config = plugin.getConfigManager().getClaimMapConfig();

                        String[] structure = {
                                        "W F F F F F F F F",
                                        "F F F F F F F F F",
                                        "F F F F F F F F F",
                                        "F F F F F F F F F",
                                        "F F F F F F F F F",
                                        "B B R B X B I B B"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('W', GuiHelper.buildSlot(config.wilderness.material, config.wilderness.name,
                                        config.wilderness.lore));
                        ingredients.put('F', GuiHelper.buildSlot(config.mapFill.material, config.mapFill.name,
                                        config.mapFill.lore));
                        ingredients.put('B', GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name,
                                        config.bottomFill.lore));
                        ingredients.put('R', GuiHelper.buildSlot(config.refresh.material, config.refresh.name,
                                        config.refresh.lore));
                        ingredients.put('X', GuiHelper.buildSlot(config.back.material, config.back.name,
                                        config.back.lore, (p, e) -> {
                                                p.closeInventory();
                                                MainMenuGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('I', GuiHelper.buildSlot(config.info.material, config.info.name,
                                        config.info.lore, (p, e) -> {
                                                p.closeInventory();
                                                ClaimMapInfoGUI.open(p, claim, plugin);
                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);

                        CustomGui gui = new CustomGui(title, 6);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
