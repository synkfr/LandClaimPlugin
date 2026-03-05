package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.TitleSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TitleToggleGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        TitleSettingsConfig config = plugin.getConfigManager().getTitleSettingsConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F T F E F O F F",
                                        "F F F F F F F F F",
                                        "F F F S < S F F F"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('S', GuiHelper.buildSlot(config.navSpacer.material, config.navSpacer.name,
                                        config.navSpacer.lore));
                        List<String> toggleLore = new ArrayList<>(config.titleToggle.lore);
                        toggleLore.add("");
                        toggleLore.add("<gray>Status: "
                                        + (profile.isEnterTitleEnabled() ? "<green>Enabled" : "<red>Disabled"));

                        ingredients.put('T', GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                                        toggleLore, (p, e) -> {
                                                profile.setEnterTitleEnabled(!profile.isEnterTitleEnabled());
                                                plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                                                open(player, profile, plugin);
                                        }));

                        List<String> entryLore = new ArrayList<>(config.onEntry.lore);
                        entryLore.add("");
                        entryLore.add("<gray>Current: <reset>" + profile.getEnterTitle());

                        ingredients.put('E', GuiHelper.buildSlot(config.onEntry.material, config.onEntry.name,
                                        entryLore, (p, e) -> {
                                                p.closeInventory();
                                                p.sendMessage(GuiHelper.MM.deserialize(plugin.getConfigManager()
                                                                .getMessagesConfig().titleEnterPrompt));
                                                org.ayosynk.landClaimPlugin.listeners.ChatInputListener.awaitInput(p,
                                                                input -> {
                                                                        if (input == null) {
                                                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                                                plugin.getConfigManager()
                                                                                                                .getMessagesConfig().titleCancelled));
                                                                        } else {
                                                                                profile.setEnterTitle(input);
                                                                                plugin.getDatabaseManager()
                                                                                                .getProfileDao()
                                                                                                .saveProfile(profile);
                                                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                                                plugin.getConfigManager()
                                                                                                                .getMessagesConfig().titleUpdated));
                                                                        }
                                                                        Bukkit.getScheduler().runTask(plugin,
                                                                                        () -> open(p, profile, plugin));
                                                                });
                                        }));

                        List<String> leaveLore = new ArrayList<>(config.onLeaveTitle.lore);
                        leaveLore.add("");
                        leaveLore.add("<gray>Current: <reset>" + profile.getLeaveTitle());

                        ingredients.put('O', GuiHelper.buildSlot(config.onLeaveTitle.material, config.onLeaveTitle.name,
                                        leaveLore, (p, e) -> {
                                                p.closeInventory();
                                                p.sendMessage(GuiHelper.MM.deserialize(plugin.getConfigManager()
                                                                .getMessagesConfig().titleLeavePrompt));
                                                org.ayosynk.landClaimPlugin.listeners.ChatInputListener.awaitInput(p,
                                                                input -> {
                                                                        if (input == null) {
                                                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                                                plugin.getConfigManager()
                                                                                                                .getMessagesConfig().titleCancelled));
                                                                        } else {
                                                                                profile.setLeaveTitle(input);
                                                                                plugin.getDatabaseManager()
                                                                                                .getProfileDao()
                                                                                                .saveProfile(profile);
                                                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                                                plugin.getConfigManager()
                                                                                                                .getMessagesConfig().titleUpdated));
                                                                        }
                                                                        Bukkit.getScheduler().runTask(plugin,
                                                                                        () -> open(p, profile, plugin));
                                                                });
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimSettingsGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
