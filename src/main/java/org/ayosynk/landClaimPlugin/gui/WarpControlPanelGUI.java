package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpControlPanelConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WarpControlPanelGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Warp warp) {
                WarpControlPanelConfig config = plugin.getConfigManager().getWarpControlPanelConfig();

                String[] structure = {
                                "f f f f f f f f f",
                                "f L s I s S s D f",
                                "f f f f < f f f f"
                };

                Map<Character, SlotDefinition> ingredients = new HashMap<>();
                ingredients.put('f', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                ingredients.put('s', GuiHelper.buildSlot(config.spacer.material, config.spacer.name, config.spacer.lore));
                ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                (p, e) -> WarpManagementGUI.open(p, profile, plugin)));

                ingredients.put('L', GuiHelper.buildSlot(config.changeLocation.material, config.changeLocation.name,
                                config.changeLocation.lore, (p, e) -> {
                                        warp.setLocation(p.getLocation());
                                        plugin.getWarpManager().setWarp(profile.getProfileId(),
                                                        warp.getName(), warp.getLocation(), warp.getIcon());
                                        p.sendMessage(plugin.getConfigManager().getMessage(
                                                        "warp-location-updated", "<name>", warp.getName()));
                                        WarpControlPanelGUI.open(p, profile, plugin, warp);
                                }));
                ingredients.put('I', GuiHelper.buildSlot(config.changeIcon.material, config.changeIcon.name,
                                config.changeIcon.lore, (p, e) -> WarpChangeIconGUI.open(p, profile, plugin, warp)));
                ingredients.put('D', GuiHelper.buildSlot(config.deleteWarp.material, config.deleteWarp.name,
                                config.deleteWarp.lore, (p, e) -> {
                                        profile.removeWarp(warp.getName());
                                        plugin.getWarpManager().deleteWarp(profile.getProfileId(), warp.getName());
                                        p.sendMessage(plugin.getConfigManager().getMessage("warp-deleted",
                                                        "<name>", warp.getName()));
                                        WarpManagementGUI.open(p, profile, plugin);
                                }));

                WarpControlPanelConfig.ItemConfig privacyConfig =
                                warp.isPublic() ? config.makePrivate : config.makePublic;
                ingredients.put('S', GuiHelper.buildSlot(privacyConfig.material, privacyConfig.name,
                                privacyConfig.lore, (p, e) -> {
                                        Boolean newValue = plugin.getWarpManager()
                                                        .toggleWarpPublic(profile.getProfileId(), warp.getName(), p);
                                        if (newValue == null) return;
                                        warp.setPublic(newValue);
                                        p.sendMessage(plugin.getConfigManager().getMessage(
                                                        newValue ? "warp-made-public" : "warp-made-private",
                                                        "<name>", warp.getName()));
                                        WarpControlPanelGUI.open(p, profile, plugin, warp);
                                }));

                Component title = GuiHelper.MM.deserialize(config.title.replace("{name}", warp.getName()));
                CustomGui gui = new CustomGui(title, 3);
                gui.fillFromStructure(structure, ingredients);
                gui.open(player);
        }
}
