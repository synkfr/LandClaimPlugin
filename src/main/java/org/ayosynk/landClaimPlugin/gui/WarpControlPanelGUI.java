package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpControlPanelConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WarpControlPanelGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Warp warp) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        WarpControlPanelConfig config = plugin.getConfigManager().getWarpControlPanelConfig();

                        String[] structure = {
                                        "f f f f f f f f f",
                                        "s L s I s D s S s",
                                        "f f f f < f f f f"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('f', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('s', GuiHelper.buildSlot(config.spacer.material, config.spacer.name,
                                        config.spacer.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                WarpManagementGUI.open(p, profile, plugin);
                                                        }));

                        ingredients.put('L',
                                        GuiHelper.buildSlot(config.changeLocation.material, config.changeLocation.name,
                                                        config.changeLocation.lore, (p, e) -> {
                                                                p.closeInventory();
                                                                warp.setLocation(p.getLocation());
                                                                plugin.getWarpManager().setWarp(profile.getOwnerId(),
                                                                                warp.getName(), warp.getLocation(),
                                                                                warp.getIcon());

                                                                p.sendMessage(plugin.getConfigManager().getMessage(
                                                                                "warp-location-updated",
                                                                                "<name>", warp.getName()));
                                                                WarpControlPanelGUI.open(p, profile, plugin, warp);
                                                        }));
                        ingredients.put('I', GuiHelper.buildSlot(config.changeIcon.material, config.changeIcon.name,
                                        config.changeIcon.lore, (p, e) -> {
                                                p.closeInventory();
                                                WarpChangeIconGUI.open(p, profile, plugin, warp);
                                        }));
                        ingredients.put('D', GuiHelper.buildSlot(config.deleteWarp.material, config.deleteWarp.name,
                                        config.deleteWarp.lore, (p, e) -> {
                                                p.closeInventory();
                                                profile.removeWarp(warp.getName());
                                                plugin.getWarpManager().deleteWarp(profile.getOwnerId(),
                                                                warp.getName());
                                                p.sendMessage(plugin.getConfigManager().getMessage("warp-deleted",
                                                                "<name>", warp.getName()));
                                                WarpManagementGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('S', GuiHelper.buildSlot("BARRIER", "<red>Warp Privacy",
                                        java.util.List.of("<gray>Coming Soon...")));

                        Component title = GuiHelper.MM.deserialize(config.title.replace("{name}", warp.getName()));
                        Bukkit.getScheduler().runTask(plugin, () -> {
                                CustomGui gui = new CustomGui(title, 3);
                                gui.fillFromStructure(structure, ingredients);
                                gui.open(player);
                        });
                });
        }
}
