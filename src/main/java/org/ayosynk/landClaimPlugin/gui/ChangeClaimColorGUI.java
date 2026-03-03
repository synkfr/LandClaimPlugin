package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ChangeClaimColorConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ChangeClaimColorGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        ChangeClaimColorConfig config = plugin.getConfigManager().getChangeClaimColorConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F 0 1 2 3 4 5 6 F",
                                        "F 7 8 9 A B C D F",
                                        "F F F E X Y F F F",
                                        "F F F N < N F F F"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frameFill.material, config.frameFill.name,
                                        config.frameFill.lore));
                        ingredients.put('N', GuiHelper.buildSlot(config.navFrame.material, config.navFrame.name,
                                        config.navFrame.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimSettingsGUI.open(p, profile, plugin);
                                                        }));
                        ingredients.put('X', GuiHelper.buildSlot(config.customColor.material, config.customColor.name,
                                        config.customColor.lore, (p, e) -> {
                                                // Reserved: Opens chat/anvil HEX input
                                        }));
                        ingredients.put('0', buildColorSlot(config.colorBlack));
                        ingredients.put('1', buildColorSlot(config.colorBlue));
                        ingredients.put('2', buildColorSlot(config.colorBrown));
                        ingredients.put('3', buildColorSlot(config.colorCyan));
                        ingredients.put('4', buildColorSlot(config.colorGray));
                        ingredients.put('5', buildColorSlot(config.colorGreen));
                        ingredients.put('6', buildColorSlot(config.colorLightBlue));
                        ingredients.put('7', buildColorSlot(config.colorLime));
                        ingredients.put('8', buildColorSlot(config.colorLightGray));
                        ingredients.put('9', buildColorSlot(config.colorMagenta));
                        ingredients.put('A', buildColorSlot(config.colorOrange));
                        ingredients.put('B', buildColorSlot(config.colorPink));
                        ingredients.put('C', buildColorSlot(config.colorPurple));
                        ingredients.put('D', buildColorSlot(config.colorRed));
                        ingredients.put('E', buildColorSlot(config.colorWhite));
                        ingredients.put('Y', buildColorSlot(config.colorYellow));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 5);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }

        private static SlotDefinition buildColorSlot(ChangeClaimColorConfig.ItemConfig itemConfig) {
                return GuiHelper.buildSlot(itemConfig.material, itemConfig.name, itemConfig.lore, (p, e) -> {
                        // Reserved: Instantly selects color
                });
        }
}
